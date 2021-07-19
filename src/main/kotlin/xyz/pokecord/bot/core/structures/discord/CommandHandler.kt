package xyz.pokecord.bot.core.structures.discord

import dev.minn.jda.ktx.CoroutineEventListener
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.LoggerFactory
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.BaseCommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.Config
import xyz.pokecord.bot.utils.PokemonOrder
import xyz.pokecord.bot.utils.PokemonResolvable
import xyz.pokecord.bot.utils.extensions.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

class CommandHandler(val bot: Bot) : CoroutineEventListener {
  private val logger = LoggerFactory.getLogger(CommandHandler::class.java)

  var prefix: String = if (bot.maintenance) "!" else "p!"

  override suspend fun onEvent(event: GenericEvent) {
    when (event) {
      is SlashCommandEvent -> onSlashCommand(event)
      is MessageReceivedEvent -> onMessageReceived(event)
    }
  }

  private suspend fun onSlashCommand(event: SlashCommandEvent) {
    if (event.isFromGuild) {
      if (!event.guild!!.selfMember.hasPermission(
          Permission.VIEW_CHANNEL,
          Permission.MESSAGE_READ,
          Permission.MESSAGE_WRITE
        )
      ) return
      if (!event.guild!!.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS)) return // TODO: send a normal text message
    }

    val context = SlashCommandContext(bot, event)
    try {
      if (!context.shouldProcess()) return
      if (bot.maintenance && !Config.devs.contains(context.author.id)) return

      var command: Command? = null
      for (module in bot.modules.values) {
        command = module.commandMap[event.name.lowercase()]
        if (command != null) break
      }

      if (command == null) return

      // TODO: subcommands

      val userData = context.getUserData()
      if (userData.blacklisted) return
      if (!userData.agreedToTerms) {
        val agreed = context.askForTOSAgreement()
        if (agreed) context.bot.database.userRepository.setAgreedToTerms(context.getUserData())
        else return
      }

      val hasRunningCommand =
        bot.cache.isRunningCommand(context.author.id) || bot.cache.getUserLock(context.author.id).isLocked
      if (hasRunningCommand) {
        context.reply(
          context.embedTemplates.error(
            "You tried to execute a command while your last command was already processing and as a result, command execution has been cancelled.",
            "Failed to execute command"
          ).build()
        ).queue()
        return
      }

      if (event.isFromGuild) {
        if (!event.guild!!.selfMember.permissions.containsAll(command.requiredClientPermissions.toList())) {
          return
        } else if (event.member != null) {
          if (!event.member!!.permissions.containsAll(command.requiredUserPermissions.toList())) {
            return
          }
        }
        // TODO: Let the user know that the bot is or they are missing required permissions
      }

      if (!command.canRun(context)) return
      // TODO: Let the user know they can't run the command?

      val cacheKey = command.getRateLimitCacheKey(context, listOf()) // TODO: args

      val rateLimitEndsAt = bot.cache.getRateLimit(cacheKey)
      if (rateLimitEndsAt != null) {
        if (rateLimitEndsAt > System.currentTimeMillis()) {
          logger.debug("User ${context.author.asTag}[${context.author.id}] hit the rate limit for the ${command.module.name}.${command.name} command.")
          context.reply(
            context.embedTemplates.error(
              context.translate("misc.errors.embeds.commandRateLimitReached.description"),
              context.translate("misc.errors.embeds.commandRateLimitReached.title")
            ).build()
          ).queue()
          return
        } else {
          bot.cache.removeRateLimit(cacheKey)
        }
      }

      val executorFunction =
        command.javaClass.kotlin.memberFunctions.find { it.annotations.any { annotation -> annotation is Command.Executor } }
          ?: return

      val parameters = executorFunction.parameters.filter { it.kind == KParameter.Kind.VALUE }
      val parsedParameters = arrayListOf<Any?>()

      for (param in parameters) {
        val commandArgumentAnnotation = param.findAnnotation<Command.Argument>()
        if (commandArgumentAnnotation != null) {
          if (event.options.isEmpty()) {
            parsedParameters.add(null)
          } else {
            val option = event.getOption(commandArgumentAnnotation.name.ifEmpty { param.name!! })
            val parsedParam: Any? = when {
              param.type.isInteger -> option?.asLong?.toInt()
              param.type.isBoolean -> option?.asBoolean
              param.type.isString -> option?.asString
              param.type.isRegex -> option?.asString?.toRegex()
              param.type.isUser -> option?.asUser
              param.type.isMember -> option?.asMember
              param.type.isRole -> option?.asRole
              param.type.isTextChannel -> option?.asGuildChannel?.let { if (it !is TextChannel) null else it }
              param.type.isVoiceChannel -> option?.asGuildChannel?.let { if (it !is VoiceChannel) null else it }
              param.type.isPokemonResolvable -> {
                val string = option?.asString
                when {
                  string == null -> PokemonResolvable.Int(null)
                  arrayOf("latest", "l").contains(string.lowercase()) -> PokemonResolvable.Latest()
                  else -> PokemonResolvable.Int(string.toIntOrNull())
                }
              }
              param.type.isPokemonOrder -> {
                val orderString = option?.asString
                when {
                  arrayOf("i", "iv").contains(orderString) -> {
                    PokemonOrder.IV
                  }
                  arrayOf("l", "lv", "level").contains(orderString) -> {
                    PokemonOrder.LEVEL
                  }
                  arrayOf("d", "dex", "pokedex").contains(orderString) -> {
                    PokemonOrder.POKEDEX
                  }
                  arrayOf("t", "time").contains(orderString) -> {
                    PokemonOrder.TIME
                  }
                  else -> PokemonOrder.DEFAULT
                }
              }
              else -> null
            }
            parsedParameters.add(parsedParam)
          }
        } else {
          when {
            param.type.isMessageCommandContext -> {
              parsedParameters.add(context)
            }
            param.type.isCommandContext -> {
              parsedParameters.add(context as ICommandContext)
            }
            param.type.isBaseCommandContext -> {
              parsedParameters.add(context as BaseCommandContext)
            }
            param.type.isMessageReceivedEvent -> {
              parsedParameters.add(event)
            }
            else -> {
              throw UnsupportedParameterException(param.name.toString())
            }
          }
        }
      }

      bot.cache.setRunningCommand(
        context.author.id,
        true
      )
      try {
        if (executorFunction.isSuspend) {
          executorFunction.callSuspend(command, *parsedParameters.toTypedArray()) // TODO: args
        } else {
          executorFunction.call(command, *parsedParameters.toTypedArray()) // TODO: args
        }
      } catch (e: Throwable) {
        context.handleException(e, command.module, command)
      }
      bot.cache.setRunningCommand(context.author.id, false)

      bot.cache.setRateLimit(
        cacheKey,
        System.currentTimeMillis() + command.rateLimit,
        command.rateLimit, TimeUnit.MILLISECONDS
      )
      if (userData.tag !== context.author.asTag) {
        bot.database.userRepository.updateTag(userData, context.author.asTag)
      }
    } catch (e: Throwable) {
      context.handleException(e)
    }
  }

  private suspend fun onMessageReceived(event: MessageReceivedEvent) {
    if (event.isFromGuild) {
      if (!event.guild.selfMember.hasPermission(
          Permission.VIEW_CHANNEL,
          Permission.MESSAGE_READ,
          Permission.MESSAGE_WRITE
        )
      ) return
      if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS)) return // TODO: send a normal text message
    }

    val context = MessageCommandContext(bot, event)
    try {
      if (!context.shouldProcess()) return
      if (bot.maintenance && !Config.devs.contains(context.author.id)) return

      val effectivePrefix = context.getPrefix()
      val splitMessage = event.message.contentRaw.split("\\s|\\n".toRegex()).toMutableList()
      var commandString = splitMessage.removeFirst()
      if (!commandString.startsWith(effectivePrefix, true)) return

      commandString = commandString.drop(effectivePrefix.length).trim().ifEmpty {
        splitMessage.removeFirstOrNull() ?: return
      }

      var command: Command? = null
      for (module in bot.modules.values) {
        command = module.commandMap[commandString.lowercase()]
        if (command != null) break
      }

      if (command != null && command.enabled) {
        if (splitMessage.size >= 1) {
          val childCommand =
            command.module.commandMap["${command.name.lowercase()}.${splitMessage.first().lowercase()}"]
          if (childCommand != null) {
            command = childCommand
            splitMessage.removeAt(0)
          }
        }
      }

      if (command == null || !command.enabled) return

      val userData = context.getUserData()
      if (userData.blacklisted) return
      if (!userData.agreedToTerms) {
        val agreed = context.askForTOSAgreement()
        if (agreed) context.bot.database.userRepository.setAgreedToTerms(context.getUserData())
        else return
      }

      val hasRunningCommand =
        bot.cache.isRunningCommand(context.author.id) || bot.cache.getUserLock(context.author.id).isLocked
      if (hasRunningCommand) {
        context.reply(
          context.embedTemplates.error(
            "You tried to execute a command while your last command was already processing and as a result, command execution has been cancelled.",
            "Failed to execute command"
          ).build()
        ).queue()
        return
      }

      if (event.isFromGuild) {
        if (!event.guild.selfMember.permissions.containsAll(command.requiredClientPermissions.toList())) {
          return
        } else if (event.member != null) {
          if (!event.member!!.permissions.containsAll(command.requiredUserPermissions.toList())) {
            return
          }
        }
        // TODO: Let the user know that the bot is or they are missing required permissions
      }

      if (!command.canRun(context)) return
      // TODO: Let the user know they can't run the command?

      val cacheKey = command.getRateLimitCacheKey(context, splitMessage)

      val rateLimitEndsAt = bot.cache.getRateLimit(cacheKey)
      if (rateLimitEndsAt != null) {
        if (rateLimitEndsAt > System.currentTimeMillis()) {
          logger.debug("User ${context.author.asTag}[${context.author.id}] hit the rate limit for the ${command.module.name}.${command.name} command.")
          context.reply(
            context.embedTemplates.error(
              context.translate("misc.errors.embeds.commandRateLimitReached.description"),
              context.translate("misc.errors.embeds.commandRateLimitReached.title")
            ).build()
          ).queue()
          return
        } else {
          bot.cache.removeRateLimit(cacheKey)
        }
      }

      val executorFunction =
        command.javaClass.kotlin.memberFunctions.find { it.annotations.any { annotation -> annotation is Command.Executor } }

      if (executorFunction != null) {
        val parameters = executorFunction.parameters.filter { it.kind == KParameter.Kind.VALUE }
        val parsedParameters = arrayListOf<Any?>()
        val argumentParser =
          ArgumentParser(context, splitMessage)

        for (param in parameters) {
          val commandArgumentAnnotation = param.findAnnotation<Command.Argument>()
          if (commandArgumentAnnotation != null) {
            if (splitMessage.isEmpty()) {
              parsedParameters.add(null)
            } else {
              val consumeRest = commandArgumentAnnotation.consumeRest
              val isPrefixed = commandArgumentAnnotation.prefixed
              val optional = commandArgumentAnnotation.optional
              val argName =
                commandArgumentAnnotation.name.ifEmpty { param.name!! }
              val argAliases = commandArgumentAnnotation.aliases
              argumentParser.provideArgumentInfo(
                consumeRest,
                isPrefixed,
                optional,
                param.type,
                argName,
                argAliases
              )
              when {
                param.type.isInteger -> {
                  parsedParameters.add(argumentParser.getInteger())
                }
                param.type.isBoolean -> {
                  parsedParameters.add(
                    argumentParser.getBoolean()
                  )
                }
                param.type.isString -> {
                  parsedParameters.add(argumentParser.getString())
                }
                param.type.isRegex -> {
                  parsedParameters.add(argumentParser.getRegex())
                }
                param.type.isUser -> {
                  parsedParameters.add(argumentParser.getUser())
                }
                param.type.isMember -> {
                  parsedParameters.add(argumentParser.getMember())
                }
                param.type.isRole -> {
                  parsedParameters.add(argumentParser.getRole())
                }
                param.type.isTextChannel -> {
                  parsedParameters.add(argumentParser.getTextChannel())
                }
                param.type.isVoiceChannel -> {
                  parsedParameters.add(argumentParser.getVoiceChannel())
                }
                param.type.isPokemonResolvable -> {
                  parsedParameters.add(argumentParser.getPokemonResolvable())
                }
                param.type.isPokemonOrder -> {
                  parsedParameters.add(argumentParser.getPokemonOrder())
                }
                else -> {
                  throw UnsupportedCommandArgumentException(param.name.toString())
                }
              }
            }
          } else {
            when {
              param.type.isMessageCommandContext -> {
                parsedParameters.add(context)
              }
              param.type.isCommandContext -> {
                parsedParameters.add(context as ICommandContext)
              }
              param.type.isBaseCommandContext -> {
                parsedParameters.add(context as BaseCommandContext)
              }
              param.type.isMessageReceivedEvent -> {
                parsedParameters.add(event)
              }
              else -> {
                throw UnsupportedParameterException(param.name.toString())
              }
            }
          }
        }

        if (userData.tag !== context.author.asTag) {
          bot.database.userRepository.updateTag(userData, context.author.asTag)
        }

        coroutineScope {
          val commandJob = launch {
            bot.cache.setRunningCommand(
              context.author.id,
              true
            )
            try {
              if (executorFunction.isSuspend) {
                executorFunction.callSuspend(command, *parsedParameters.toTypedArray())
              } else {
                executorFunction.call(command, *parsedParameters.toTypedArray())
              }
            } catch (e: Throwable) {
              context.handleException(e, command.module, command)
            }
            bot.cache.setRunningCommand(context.author.id, false)
          }

          launch {
            delay(1000)
            while (!commandJob.isCompleted) {
              event.channel.sendTyping().queue()
              delay(5000)
            }
          }

          launch {
            val startedAt = System.currentTimeMillis()
            while (!commandJob.isCompleted) {
              delay(1000)
              if (System.currentTimeMillis() - startedAt >= 60_000) {
                commandJob.cancelAndJoin()
                context.reply(
                  context.embedTemplates.error(
                    context.translate("misc.texts.commandTimedOut")
                  ).build()
                ).queue()
                break
              }
            }
          }
        }

        bot.cache.setRateLimit(
          cacheKey,
          System.currentTimeMillis() + command.rateLimit,
          command.rateLimit, TimeUnit.MILLISECONDS
        )
      }
    } catch (e: Throwable) {
      context.handleException(e)
    }
  }

  private class UnsupportedParameterException(parameterName: String) :
    Exception("Parameter $parameterName had an unsupported type.")

  private class UnsupportedCommandArgumentException(parameterName: String) :
    Exception("Command argument $parameterName had an unsupported type.")
}
