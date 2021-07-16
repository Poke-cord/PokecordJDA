package xyz.pokecord.bot.modules.general.events

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import xyz.pokecord.bot.core.managers.I18n
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.discord.base.Event
import xyz.pokecord.bot.modules.developer.DeveloperCommand
import xyz.pokecord.bot.utils.Config
import xyz.pokecord.bot.utils.extensions.asOptionType
import xyz.pokecord.bot.utils.extensions.awaitSuspending
import xyz.pokecord.bot.utils.extensions.removeAccents
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

class ReadyEvent : Event() {
  override val name = "Ready"

  private suspend fun prepareSlashCommands(jda: JDA) {
    if (System.getenv("REGISTER_SLASH_COMMANDS") != null) {
      val commandsData = module.bot.modules.map { (_, module) ->
        module.commands.mapNotNull { command ->
          if (command is DeveloperCommand) return@mapNotNull null // TODO: add check for mod commands
          val commandDescription = I18n.translate(null, command.descriptionI18nKey, "")
          if (commandDescription == "") return@mapNotNull null
          val commandData = CommandData(
            command.name.lowercase(),
            commandDescription
          )

          val executorFunction =
            command.javaClass.kotlin.memberFunctions.find { it.annotations.any { annotation -> annotation is Command.Executor } }!!

          val parameters = executorFunction.parameters.filter { it.kind == KParameter.Kind.VALUE }

          for (param in parameters) {
            val commandArgumentAnnotation = param.findAnnotation<Command.Argument>() ?: continue
            commandData.addOption(
              param.asOptionType,
              commandArgumentAnnotation.name.ifEmpty { param.name!! }.removeAccents(),
              commandArgumentAnnotation.description,
              !commandArgumentAnnotation.optional
            )
          }
          commandData
        }
      }.flatten()

      if (module.bot.devEnv) {
        module.bot.shardManager.getGuildById(Config.testingServer)?.let {
          commandsData.forEach { commandData ->
            it.upsertCommand(commandData).await()
          }
        }
      } else {
        commandsData.forEach { commandData ->
          jda.upsertCommand(commandData).await()
        }
      }
    }
  }

  @Handler
  suspend fun onReady(event: ReadyEvent) {
    prepareSlashCommands(event.jda)

    // Delete existing shard status when shard 0 logs in
    if (event.jda.shardInfo.shardId == 0) {
      module.bot.cache.shardStatusMap.deleteAsync().awaitSuspending()
    }

    module.bot.logger.info("Logged in as ${event.jda.selfUser.asTag} (shard ${event.jda.shardInfo.shardId})!")
    module.bot.updatePresence()
  }
}
