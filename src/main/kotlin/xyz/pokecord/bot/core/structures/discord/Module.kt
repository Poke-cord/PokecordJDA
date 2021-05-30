package xyz.pokecord.bot.core.structures.discord

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.requests.GatewayIntent
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaType

@Suppress("UNUSED", "COULD_BE_PRIVATE")
abstract class Module(
  val bot: Bot,
  val commands: Array<Command> = arrayOf(),
  val events: Array<Event> = arrayOf(),
  private val jobs: Array<Job> = arrayOf(),
  val intents: Array<GatewayIntent> = GatewayIntent.getIntents(GatewayIntent.DEFAULT).toTypedArray()
) : EventListener {
  abstract val name: String

  val commandMap = linkedMapOf<String, Command>()
  open var enabled = true
  private val eventMap = hashMapOf<String, Event>()
  private val jobMap = hashMapOf<String, Job>()

  fun load() {
    for (command in commands) {
      val executorFunction =
        command.javaClass.kotlin.memberFunctions.find { it.annotations.any { annotation -> annotation is Command.Executor } }
          ?: continue

      var allConsumed = false

      for (param in executorFunction.parameters.filter { it.kind == KParameter.Kind.VALUE }) {
        val commandArgumentAnnotation = param.findAnnotation<Command.Argument>()
        if (commandArgumentAnnotation != null) {
          if (allConsumed) {
            throw IllegalArgumentException("Command arguments cannot appear after another command argument that has consumeRest = true.")
          }
          if (!param.type.isMarkedNullable) {
            throw IllegalArgumentException("All command arguments must be nullable.")
          }
          if (commandArgumentAnnotation.consumeRest) allConsumed = true
        }
      }
      command.module = this
      if (!command::class.hasAnnotation<Command.ChildCommand>()) {
        this.commandMap[command.name.toLowerCase()] = command
        for (alias in command.aliases) {
          this.commandMap[alias.toLowerCase()] = command
        }
        if (command is ParentCommand) {
          val childCommandClasses = command::class.nestedClasses.filter { it.hasAnnotation<Command.ChildCommand>() }
          for (childCommandClass in childCommandClasses) {
            val childCommand = command.module.commands.find { it::class == childCommandClass }
            if (childCommand != null) {
              childCommand.parentCommand = command
              command.childCommands.add(childCommand)
              this.commandMap["${command.name.toLowerCase()}.${childCommand.name.toLowerCase()}"] = childCommand
              for (alias in childCommand.aliases) {
                this.commandMap["${command.name.toLowerCase()}.${alias.toLowerCase()}"] = childCommand
              }
            }
          }
        }
      }
    }
    for (event in events) {
      event.module = this
      this.eventMap[event.name] = event
      event.onLoad()
    }
    for (job in jobs) {
      job.module = this
      jobMap[job.name] = job
    }
  }

  override fun onEvent(event: GenericEvent) {
    if (!enabled) return
    if (event is ReadyEvent) {
      for (job in jobs) {
        if (job.enabled) job.start()
      }
    }
    for (ev in events) {
      if (!ev.enabled) continue
      val handlerFunctions =
        ev.javaClass.kotlin.memberFunctions.filter { it.annotations.any { annotation -> annotation is Event.Handler } }
      for (handlerFunction in handlerFunctions) {
        val firstParam = handlerFunction.parameters.find { it.kind != KParameter.Kind.INSTANCE } ?: return
        val eventToHandle =
          if (firstParam.type.javaType == event::class.java) event
          else if (firstParam.type.javaType == MessageReceivedContext::class.java && event is MessageReceivedEvent) MessageReceivedContext(
            bot,
            event.jda,
            event.responseNumber,
            event.message
          ) else null
        if (eventToHandle != null) {
          if (handlerFunction.isSuspend) {
            GlobalScope.launch {
              handlerFunction.callSuspend(ev, eventToHandle)
            }
          } else {
            handlerFunction.call(ev, eventToHandle)
          }
        }
      }
    }
  }
}
