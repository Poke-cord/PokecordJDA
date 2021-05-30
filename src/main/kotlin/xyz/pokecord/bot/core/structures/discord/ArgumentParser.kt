package xyz.pokecord.bot.core.structures.discord

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.entities.*
import xyz.pokecord.bot.utils.PokemonResolvable
import xyz.pokecord.bot.utils.extensions.isBoolean
import kotlin.reflect.KType

class ArgumentParser(
  private val context: MessageReceivedContext,
  private var args: MutableList<String>
) {
  private val channelMentionRegex = Regex("^<#?(?<id>\\d+)>$")
  private val quotedStringRegex = Regex("(['\"])([^('|\")]+)(['\"])")
  private val roleMentionRegex = Regex("^<@&(?<id>\\d+)>$")
  private val userAndMemberMentionRegex = Regex("^<@!?(?<id>\\d+)>$")

  private val nextString
    get() = args.firstOrNull()

  private lateinit var paramType: KType
  private var consumeRest: Boolean = false
  private var isPrefixed: Boolean = false
  private var optional: Boolean = false
  private var argName: String? = null
  private var argAliases: Array<String> = arrayOf()

  fun provideArgumentInfo(
    consumeRest: Boolean? = null,
    isPrefixed: Boolean? = null,
    optional: Boolean? = null,
    paramType: KType? = null,
    argName: String? = null,
    argAliases: Array<String>? = null
  ) {
    this.consumeRest = consumeRest ?: this.consumeRest
    this.isPrefixed = isPrefixed ?: this.isPrefixed
    this.optional = optional ?: this.optional
    this.paramType = paramType ?: this.paramType
    this.argName = argName ?: this.argName
    this.argAliases = argAliases ?: this.argAliases
  }

  fun getString() = nextArg(true)

  private fun nextArg(consume: Boolean = !optional): String? {
    return if (consumeRest) {
      if (isPrefixed) {
        if (argName?.equals(nextString, true) == true || argAliases.any { alias -> nextString.equals(alias, true) }) {
          args.removeAt(0)
        } else return null
      }
      args.joinToString(" ")
    } else {
      var arg: String = (if (isPrefixed && argName != null) {
        val argIndex = args.indexOfFirst {
          it.equals(argName, true) || argAliases.any { alias ->
            it.equals(
              alias,
              true
            )
          }
        }
        if (argIndex >= 0) {
          val arg = if (paramType.isBoolean) "true" else args.getOrNull(argIndex + 1)
          if (!paramType.isBoolean) args.removeAt(argIndex)
          if (args.isNotEmpty()) args.removeAt(argIndex)
          arg
        } else null
      } else nextString) ?: return null
      if (!isPrefixed) {
        if (arg.startsWith("\"") || arg.startsWith("'")) {
          val joinedArgs = args.joinToString(" ")
          val argFromQuotes = quotedStringRegex.find(
            joinedArgs
          )?.groupValues?.getOrNull(0)
          arg = argFromQuotes?.drop(1)?.dropLast(1) ?: arg
          if (consume) {
            args =
              argFromQuotes?.let { joinedArgs.removePrefix(it).trim().split("[\\s&&[^\\n]]++").toMutableList() } ?: args
          }
        } else if (consume) {
          args = args.drop(1).toMutableList()
        }
      }
      arg
    }
  }

  fun getBoolean(): Boolean? {
    val arg = nextArg()
    val bool =
      when {
        arg.equals("true", true) -> true
        arg.equals("false", true) -> false
        else -> null
      }
    optionalArgCleanup(bool)
    return bool
  }

  fun getInteger(): Int? {
    val arg = nextArg()
    val int =
      if (arg == null) null
      else arg.toIntOrNull()
    optionalArgCleanup(int)
    return int
  }

  fun getRegex(): Regex? {
    val arg = nextArg()
    val regex =
      if (arg == null) null
      else {
        val parts = arg.split("/").drop(1)
        val pattern = parts.firstOrNull() ?: ""
        val optionsString = parts.drop(1).take(1).first()
        val options = mutableSetOf<RegexOption>()
        if (optionsString.contains("i")) {
          options.add(RegexOption.IGNORE_CASE)
        }
        pattern.toRegex(options)
      }
    optionalArgCleanup(regex)
    return regex
  }

  fun getTextChannel(): TextChannel? {
    val arg = nextArg()
    val textChannel =
      if (arg == null) null
      else {
        val id = getIdFromMention(channelMentionRegex, arg.trim())
        if (id != null) context.guild.getTextChannelById(id)
        else context.guild.getTextChannelsByName(arg.trim(), true).firstOrNull()
      }
    optionalArgCleanup(textChannel)
    return textChannel
  }

  fun getVoiceChannel(): VoiceChannel? {
    val arg = nextArg()
    val voiceChannel =
      if (arg == null) null
      else {
        val id = getIdFromMention(null, arg.trim())
        if (id != null) context.guild.getVoiceChannelById(id)
        else context.guild.getVoiceChannelsByName(arg.trim(), true).firstOrNull()
      }
    optionalArgCleanup(voiceChannel)
    return voiceChannel
  }

  suspend fun getMember(): Member? {
    val arg = nextArg()
    val member =
      if (arg == null) null
      else {
        if (!context.isFromGuild) null
        else {
          val id = getIdFromMention(userAndMemberMentionRegex, arg.trim())
          if (id != null) {
            context.guild.retrieveMemberById(id).await()
          } else {
            context.guild.getMemberByTag(arg.trim())
          }
        }
      }
    optionalArgCleanup(member)
    return member
  }

  fun getRole(): Role? {
    val arg = nextArg()
    val role =
      if (arg == null) null
      else {
        if (!context.isFromGuild) null
        else {
          val id = getIdFromMention(roleMentionRegex, arg.trim())
          if (id != null) {
            context.guild.getRoleById(id)
          } else {
            context.guild.getRolesByName(arg.trim(), true).firstOrNull()
          }
        }
      }
    optionalArgCleanup(role)
    return role
  }

  suspend fun getUser(): User? {
    val arg = nextArg()
    val user =
      if (arg == null) null
      else {
        val id = getIdFromMention(userAndMemberMentionRegex, arg)
        if (id != null) context.jda.retrieveUserById(id).await()
        else {
          try {
            context.jda.getUserByTag(arg)
          } catch (e: IllegalArgumentException) {
            null
          }
        }
      }
    optionalArgCleanup(user)
    return user
  }

  fun getPokemonResolvable(): PokemonResolvable {
    val string = nextArg()
    val pokemonResolvable =
      when {
        string == null -> PokemonResolvable.Int(null)
        arrayOf("latest", "l").contains(
          string.toLowerCase()
        ) -> PokemonResolvable.Latest()
        else -> PokemonResolvable.Int(string.toIntOrNull())
      }
    optionalArgCleanup(pokemonResolvable.data)
    return pokemonResolvable
  }

  private fun <T> optionalArgCleanup(data: T?) {
    if (!isPrefixed && optional && data != null) nextArg(true)
  }

  private fun getIdFromMention(regex: Regex?, input: String): Long? {
    if (input.toIntOrNull() != null) return null
    val longValue = input.toLongOrNull()
    if (longValue != null) return longValue
    if (regex == null) return null
    val match = regex.matchEntire(input)
    return if (match != null) {
      match.groupValues[1].toLongOrNull()
    } else null
  }
}
