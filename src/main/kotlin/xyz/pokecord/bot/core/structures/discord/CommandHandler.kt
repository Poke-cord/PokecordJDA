package xyz.pokecord.bot.core.structures.discord

import dev.minn.jda.ktx.await
import dev.minn.jda.ktx.awaitMessage
import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.BaseCommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.Config
import xyz.pokecord.bot.utils.PokemonResolvable
import xyz.pokecord.bot.utils.extensions.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

class CommandHandler(val bot: Bot) : ListenerAdapter() {
  private val logger = LoggerFactory.getLogger(CommandHandler::class.java)

  var prefix: String = if (bot.maintenance) "!" else "p!"

  override fun onSlashCommand(event: SlashCommandEvent) {
    val context = SlashCommandContext(bot, event)
    if (context.author.isBot) return
    if (!context.shouldProcess()) return
    if (bot.maintenance && !Config.devs.contains(context.author.id)) return

    var command: Command? = null
    for (module in bot.modules.values) {
      command = module.commandMap[event.name.toLowerCase()]
      if (command != null) break
    }

    if (command == null) return

    // TODO: subcommands

    GlobalScope.launch {
      val userData = context.getUserData()
      if (userData.blacklisted) return@launch
      if (!userData.agreedToTerms) {
        context.reply(
          context.embedTemplates.normal(
            context.translate(
              "misc.embeds.rules.description",
              mapOf(
                "user" to context.author.asMention,
                "tosUrl" to "https://pokecord.xyz/rules"
              )
            )
          ).build(),
          true
        ).await()
        val responseMessage = event.channel.awaitMessage(context.author)
        if (!responseMessage.contentRaw.equals(context.author.id.asTrainerId.toInt().toString(16).reversed(), true)) {
          return@launch
        }
        context.bot.database.userRepository.setAgreedToTerms(context.getUserData())
      }

      val hasRunningCommand = bot.cache.isRunningCommand(context.author.id)
      if (hasRunningCommand) {
        context.reply(
          context.embedTemplates.error(
            "You tried to execute a command while your last command was already processing and as a result, command execution has been cancelled.",
            "Failed to execute command"
          ).build()
        ).queue()
        return@launch
      }

      if (event.isFromGuild) {
        if (!event.guild!!.selfMember.permissions.containsAll(command.requiredClientPermissions.toList())) {
          return@launch
        } else if (event.member != null) {
          if (!event.member!!.permissions.containsAll(command.requiredUserPermissions.toList())) {
            return@launch
          }
        }
        // TODO: Let the user know that the bot is or they are missing required permissions
      }

      if (!command.canRun(context)) return@launch
      // TODO: Let the user know they can't run the command?

      val cacheKey = command.getRateLimitCacheKey(context, listOf()) // TODO: args

      val rateLimitEndsAt = bot.cache.getRateLimit(cacheKey)
      if (rateLimitEndsAt != null) {
        if (rateLimitEndsAt > System.currentTimeMillis()) {
          // TODO: handle rate limit hit
          logger.debug("User ${context.author.asTag}[${context.author.id}] hit the rate limit for the ${command.module.name}.${command.name} command.")
        } else {
          bot.cache.removeRateLimit(cacheKey)
        }
      }

      val executorFunction =
        command.javaClass.kotlin.memberFunctions.find { it.annotations.any { annotation -> annotation is Command.Executor } }

      if (executorFunction != null) {
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
                    arrayOf("latest", "l").contains(string.toLowerCase()) -> PokemonResolvable.Latest()
                    else -> PokemonResolvable.Int(string.toIntOrNull())
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
//        TODO: args
//        val argumentParser =
//          ArgumentParser(context, args)
//        for (param in parameters) {
//          val commandArgumentAnnotation = param.findAnnotation<Command.Argument>()
//          if (commandArgumentAnnotation != null) {
//            if (args.isEmpty()) {
//              parsedParameters.add(null)
//            } else {
//              val consumeRest = commandArgumentAnnotation.consumeRest
//              val isPrefixed = commandArgumentAnnotation.prefixed
//              val optional = commandArgumentAnnotation.optional
//              val argName =
//                commandArgumentAnnotation.name.ifEmpty { param.name!! }
//              val argAliases = commandArgumentAnnotation.aliases
//              argumentParser.provideArgumentInfo(
//                consumeRest,
//                isPrefixed,
//                optional,
//                param.type,
//                argName,
//                argAliases
//              )
//              when {
//                param.type.isInteger -> {
//                  parsedParameters.add(argumentParser.getInteger())
//                }
//                param.type.isBoolean -> {
//                  parsedParameters.add(
//                    argumentParser.getBoolean()
//                  )
//                }
//                param.type.isString -> {
//                  parsedParameters.add(argumentParser.getString())
//                }
//                param.type.isRegex -> {
//                  parsedParameters.add(argumentParser.getRegex())
//                }
//                param.type.isUser -> {
//                  parsedParameters.add(argumentParser.getUser())
//                }
//                param.type.isMember -> {
//                  parsedParameters.add(argumentParser.getMember())
//                }
//                param.type.isRole -> {
//                  parsedParameters.add(argumentParser.getRole())
//                }
//                param.type.isTextChannel -> {
//                  parsedParameters.add(argumentParser.getTextChannel())
//                }
//                param.type.isVoiceChannel -> {
//                  parsedParameters.add(argumentParser.getVoiceChannel())
//                }
//                param.type.isPokemonResolvable -> {
//                  parsedParameters.add(argumentParser.getPokemonResolvable())
//                }
//                else -> {
//                  throw UnsupportedCommandArgumentException(param.name.toString())
//                }
//              }
//            }
//          } else {
//            when {
//              param.type.isMessageCommandContext -> {
//                parsedParameters.add(context)
//              }
//              param.type.isMessageReceivedEvent -> {
//                parsedParameters.add(event)
//              }
//              else -> {
//                throw UnsupportedParameterException(param.name.toString())
//              }
//            }
//          }
//        }
        val commandJob = launch(Dispatchers.Default) {
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
          } catch (e: Exception) {
            context.handleException(e, command.module, command)
          }
          bot.cache.setRunningCommand(context.author.id, false)
        }

        launch(Dispatchers.Default + CoroutineName("TimeoutHandler")) {
          val startedAt = System.currentTimeMillis()
          while (!commandJob.isCompleted) {
            delay(1000)
            if (System.currentTimeMillis() - startedAt >= 60_000) {
              commandJob.cancelAndJoin()
              context.reply(context.translate("misc.texts.commandTimedOut")).queue()
              break
            }
          }
        }

        withContext(Dispatchers.IO)
        {
          bot.cache.setRateLimit(
            cacheKey,
            System.currentTimeMillis() + command.rateLimit,
            command.rateLimit, TimeUnit.MILLISECONDS
          )
          if (userData.tag !== context.author.asTag) {
            bot.database.userRepository.updateTag(userData, context.author.asTag)
          }
        }
      }
    }
  }

  @Suppress("UNUSED")
  override fun onMessageReceived(event: MessageReceivedEvent) {
    val context = MessageCommandContext(bot, event)
    if (context.author.isBot) return
    if (!context.shouldProcess()) return
    if (bot.maintenance && !Config.devs.contains(context.author.id)) return

    GlobalScope.launch {
      val effectivePrefix = context.getPrefix()
      var splitMessage = event.message.contentRaw.split("\\s|\\n".toRegex()).toMutableList()
      var commandString = splitMessage.removeFirst()
      if (!commandString.startsWith(effectivePrefix, true)) return@launch

      commandString = commandString.drop(effectivePrefix.length).trim().ifEmpty {
        splitMessage.removeFirstOrNull() ?: return@launch
      }

      var command: Command? = null
      for (module in bot.modules.values) {
        command = module.commandMap[commandString.toLowerCase()]
        if (command != null) break
      }

      if (command != null && command.enabled) {
        if (splitMessage.size >= 2) {
          val childCommand = command.module.commandMap["${command.name.toLowerCase()}.${splitMessage[1].toLowerCase()}"]
          if (childCommand != null) {
            command = childCommand
            splitMessage = splitMessage.also { it.removeAt(1) }
          }
        }
      }

      if (command == null || !command.enabled) return@launch

      val userData = context.getUserData()
      if (userData.blacklisted) return@launch
      if (!userData.agreedToTerms) {
        context.reply(
          context.embedTemplates.normal(
            context.translate(
              "misc.embeds.rules.description",
              mapOf(
                "user" to context.author.asMention,
                "tosUrl" to "https://pokecord.xyz/rules"
              )
            )
          ).build(),
          true
        ).await()
        val responseMessage = event.channel.awaitMessage(context.author)
        if (!responseMessage.contentRaw.equals(context.author.id.asTrainerId.toInt().toString(16).reversed(), true)) {
          return@launch
        }
        context.bot.database.userRepository.setAgreedToTerms(context.getUserData())
      }

      val hasRunningCommand = bot.cache.isRunningCommand(context.author.id)
      if (hasRunningCommand) {
        context.reply(
          context.embedTemplates.error(
            "You tried to execute a command while your last command was already processing and as a result, command execution has been cancelled.",
            "Failed to execute command"
          ).build()
        ).queue()
        return@launch
      }

      if (event.isFromGuild) {
        if (!event.guild.selfMember.permissions.containsAll(command.requiredClientPermissions.toList())) {
          return@launch
        } else if (event.member != null) {
          if (!event.member!!.permissions.containsAll(command.requiredUserPermissions.toList())) {
            return@launch
          }
        }
        // TODO: Let the user know that the bot is or they are missing required permissions
      }

      if (!command.canRun(context)) return@launch
      // TODO: Let the user know they can't run the command?

      val args =
        if (splitMessage.size == 1) mutableListOf() else splitMessage.slice(IntRange(1, splitMessage.size - 1))
          .toMutableList()

      val cacheKey = command.getRateLimitCacheKey(context, args)

      val rateLimitEndsAt = bot.cache.getRateLimit(cacheKey)
      if (rateLimitEndsAt != null) {
        if (rateLimitEndsAt > System.currentTimeMillis()) {
          // TODO: handle rate limit hit
          logger.debug("User ${context.author.asTag}[${context.author.id}] hit the rate limit for the ${command.module.name}.${command.name} command.")
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
          ArgumentParser(context, args)

        for (param in parameters) {
          val commandArgumentAnnotation = param.findAnnotation<Command.Argument>()
          if (commandArgumentAnnotation != null) {
            if (args.isEmpty()) {
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
        val commandJob = launch(Dispatchers.Default) {
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
          } catch (e: Exception) {
            context.handleException(e, command.module, command)
          }
          bot.cache.setRunningCommand(context.author.id, false)
        }

        launch(Dispatchers.Default + CoroutineName("StartTyping")) {
          delay(1000)
          while (!commandJob.isCompleted) {
            event.channel.sendTyping().queue()
            delay(5000)
          }
        }

        launch(Dispatchers.Default + CoroutineName("TimeoutHandler")) {
          val startedAt = System.currentTimeMillis()
          while (!commandJob.isCompleted) {
            delay(1000)
            if (System.currentTimeMillis() - startedAt >= 60_000) {
              commandJob.cancelAndJoin()
              context.reply(context.translate("misc.texts.commandTimedOut")).queue()
              break
            }
          }
        }

        withContext(Dispatchers.IO)
        {
          bot.cache.setRateLimit(
            cacheKey,
            System.currentTimeMillis() + command.rateLimit,
            command.rateLimit, TimeUnit.MILLISECONDS
          )
          if (userData.tag !== context.author.asTag) {
            bot.database.userRepository.updateTag(userData, context.author.asTag)
          }
        }
      }
    }
  }

  private class UnsupportedParameterException(parameterName: String) :
    Exception("Parameter $parameterName had an unsupported type.")

  private class UnsupportedCommandArgumentException(parameterName: String) :
    Exception("Command argument $parameterName had an unsupported type.")
}
