package xyz.pokecord.bot.utils.extensions

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.MessageCommandContext
import xyz.pokecord.bot.core.structures.discord.SlashCommandContext
import xyz.pokecord.bot.core.structures.discord.base.BaseCommandContext
import xyz.pokecord.bot.utils.PokemonResolvable
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

val KParameter.asOptionType
  get() = when {
    type.isBoolean -> OptionType.BOOLEAN
    type.isInteger -> OptionType.INTEGER
    type.isString -> OptionType.STRING
    type.isRegex -> OptionType.STRING

    type.isMember -> OptionType.USER
    type.isRole -> OptionType.ROLE
    type.isTextChannel -> OptionType.CHANNEL
    type.isUser -> OptionType.USER
    type.isVoiceChannel -> OptionType.CHANNEL
    type.isPokemonResolvable -> OptionType.STRING

    else -> OptionType.UNKNOWN
  }

val KType.isBoolean
  get() = javaType === java.lang.Boolean::class.java || javaType === Boolean::class.javaPrimitiveType
val KType.isInteger
  get() = javaType === java.lang.Integer::class.java || javaType === Int::class.javaPrimitiveType
val KType.isString
  get() = javaType === java.lang.String::class.java || javaType === String::class
val KType.isRegex
  get() = javaType === Regex::class.java

val KType.isMember
  get() = javaType === Member::class.java
val KType.isRole
  get() = javaType === Role::class.java
val KType.isTextChannel
  get() = javaType === TextChannel::class.java
val KType.isUser
  get() = javaType === User::class.java
val KType.isVoiceChannel
  get() = javaType === VoiceChannel::class.java
val KType.isPokemonResolvable
  get() = javaType === PokemonResolvable::class.java

val KType.isCommandContext
  get() = javaType === ICommandContext::class.java
val KType.isBaseCommandContext
  get() = javaType === BaseCommandContext::class.java
val KType.isMessageCommandContext
  get() = javaType === MessageCommandContext::class.java
val KType.isSlashCommandContext
  get() = javaType === SlashCommandContext::class.java
val KType.isMessageReceivedEvent
  get() = javaType === MessageReceivedEvent::class.java
val KType.isSentryScope
  get() = javaType === io.sentry.Scope::class.java
