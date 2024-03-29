package xyz.pokecord.bot.core.structures.discord.base

import dev.minn.jda.ktx.CoroutineEventListener
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import org.slf4j.LoggerFactory
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.MessageCommandContext
import xyz.pokecord.bot.utils.extensions.isMessageCommandContext
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaType

@Suppress("UNUSED", "COULD_BE_PRIVATE")
abstract class Module(
  val bot: Bot,
  val commands: MutableList<Command> = mutableListOf(),
  val events: Array<Event> = arrayOf(),
  private val tasks: Array<Task> = arrayOf(),
  val intents: Array<GatewayIntent> = GatewayIntent.getIntents(GatewayIntent.DEFAULT).toTypedArray()
) : CoroutineEventListener {
  constructor(
    bot: Bot,
    commands: Array<Command> = arrayOf(),
    events: Array<Event> = arrayOf(),
    tasks: Array<Task> = arrayOf(),
    intents: Array<GatewayIntent> = GatewayIntent.getIntents(GatewayIntent.DEFAULT).toTypedArray()
  ) : this(bot, mutableListOf(*commands), events, tasks, intents)

  abstract val name: String

  val commandMap = linkedMapOf<String, Command>()
  open var enabled = true
  private val eventMap = hashMapOf<String, Event>()
  private val taskMap = hashMapOf<String, Task>()

  fun load() {
    for (command in commands) {
      addCommand(command)
    }
    for (event in events) {
      event.module = this
      this.eventMap[event.name] = event
      event.onLoad()
    }
    for (task in tasks) {
      task.module = this
      taskMap[task.name] = task
    }
  }

  @Suppress("COULD_BE_PRIVATE")
  fun addCommand(command: Command) {
    val executorFunction =
      command.javaClass.kotlin.memberFunctions.find { it.annotations.any { annotation -> annotation is Command.Executor } }

    if (executorFunction == null) {
      logger.warn("${command::class.java.name} doesn't have a function annotated with ${Command.Executor::class.java.name}")
      return
    }

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
    command.identifiersForCommandHandler.forEach {
      commandMap[it] = command
    }
    if (command is ParentCommand) {
      for (childCommand in command.childCommands) {
        childCommand.parentCommand = command
        addCommand(childCommand)
      }
    }
  }

  override suspend fun onEvent(event: GenericEvent) {
    if (!enabled) return

    if (event is ReadyEvent) {
      for (task in tasks) {
        if (task.enabled && !task.started) task.start()
      }
    }
    for (ev in events) {
      if (!ev.enabled) continue
      val handlerFunctions =
        ev.javaClass.kotlin.memberFunctions.filter { it.annotations.any { annotation -> annotation is Event.Handler } }
      for (handlerFunction in handlerFunctions) {
        val firstParam = handlerFunction.parameters.find { it.kind != KParameter.Kind.INSTANCE } ?: return
        // TODO: make sure it functions properly
//        val eventToHandle = event
//          if (firstParam.type.javaType == event::class.java) event
//          else if (firstParam.type.javaType == MessageCommandContext::class.java && event is MessageReceivedEvent) MessageCommandContext(
//            bot,
//            event
//          ) else null
//        if (eventToHandle != null) {
        try {
          if (firstParam.type.javaType == event::class.java) {
            if (handlerFunction.isSuspend) {
              coroutineScope {
                launch {
                  handlerFunction.callSuspend(ev, event)
                }
              }
            } else {
              handlerFunction.call(ev, event)
            }
          } else if (firstParam.type.isMessageCommandContext && event::class.java == MessageReceivedEvent::class.java) {
            val context = MessageCommandContext(bot, event as MessageReceivedEvent)
            if (handlerFunction.isSuspend) {
              coroutineScope {
                launch {
                  handlerFunction.callSuspend(ev, context)
                }
              }
            } else {
              handlerFunction.call(ev, context)
            }
          }
        } catch (e: Throwable) {
          e.printStackTrace()
        }
//        }
      }
    }
  }

  companion object {
    private val logger = LoggerFactory.getLogger(Module::class.java)
  }
}
