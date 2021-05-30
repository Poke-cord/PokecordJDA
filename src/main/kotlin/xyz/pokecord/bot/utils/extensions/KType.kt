package xyz.pokecord.bot.utils.extensions

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext
import xyz.pokecord.bot.utils.PokemonResolvable
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

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

val KType.isExtendedMessageReceivedEvent
  get() = javaType === MessageReceivedContext::class.java
val KType.isMessageReceivedEvent
  get() = javaType === MessageReceivedEvent::class.java
val KType.isSentryScope
  get() = javaType === io.sentry.Scope::class.java
val KType.isPokemonResolvable
  get() = javaType === PokemonResolvable::class.java
