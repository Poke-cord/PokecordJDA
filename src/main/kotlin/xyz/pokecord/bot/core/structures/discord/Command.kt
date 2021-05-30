package xyz.pokecord.bot.core.structures.discord

import net.dv8tion.jda.api.Permission
import xyz.pokecord.bot.utils.extensions.isBoolean
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

abstract class Command {
  @Target(AnnotationTarget.FUNCTION)
  @Retention(AnnotationRetention.RUNTIME)
  annotation class Executor

  @Target(AnnotationTarget.VALUE_PARAMETER)
  @Retention(AnnotationRetention.RUNTIME)
  annotation class Argument(
    val consumeRest: Boolean = false,
    val name: String = "",
    val aliases: Array<String> = [],
    val optional: Boolean = false,
    val prefixed: Boolean = false
  )

  @Target(AnnotationTarget.CLASS)
  @Retention(AnnotationRetention.RUNTIME)
  annotation class ChildCommand

  enum class RateLimitType {
    Command,
    Args
  }

  abstract val name: String

  lateinit var module: Module
  var parentCommand: Command? = null

  open var aliases: Array<String> = arrayOf()
  open var enabled = true
  open var excludeFromHelp = false
  open var rateLimit = 1500L
  open var rateLimitType = RateLimitType.Command
  open var requiredClientPermissions: Array<Permission> =
    arrayOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)
  open var requiredUserPermissions: Array<Permission> = arrayOf(Permission.MESSAGE_READ)

  open val usage: String by lazy {
    this::class.memberFunctions.forEach { function ->
      val executorAnnotation = function.findAnnotation<Executor>()
      if (executorAnnotation != null) {
        var usageString = ""
        function.parameters.forEach { parameter ->
          val argumentAnnotation = parameter.findAnnotation<Argument>()
          if (argumentAnnotation != null) {
            if (usageString.isEmpty()) usageString += " "
            var prefix = if (argumentAnnotation.optional) "[" else "<"
            var suffix = if (argumentAnnotation.optional) "]" else ">"
            var argName = argumentAnnotation.name.ifEmpty { parameter.name }
            if (parameter.type.isBoolean) {
              if (argumentAnnotation.optional) {
                prefix = "["
                suffix = "]"
              } else {
                argName += " (true/false)"
              }
            }
            usageString += "$prefix${argName}$suffix "
          }
        }
        return@lazy usageString.trim()
      }
    }
    return@lazy ""
  }

  open fun canRun(context: MessageReceivedContext): Boolean {
    return true
  }

  open fun getRateLimitCacheKey(context: MessageReceivedContext, args: List<String>): String {
    return (if (rateLimitType == RateLimitType.Command) "${context.author.id}.${module.name}.${name}" else "${context.author.id}.${module.name}.${name}.${
      args.joinToString(" ")
    }").toLowerCase()
  }
}
