package xyz.pokecord.bot.modules.developer.commands

import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext
import xyz.pokecord.bot.modules.developer.DeveloperCommand

class ShowCommand : DeveloperCommand() {
  override val name = "Show"

  @Executor
  suspend fun execute(context: MessageReceivedContext) {
    if (!context.isFromGuild) return
    val spawnChannels = module.bot.database.spawnChannelRepository.getSpawnChannels(context.guild.id)

    context.reply(
      context.embedTemplates.normal(
        if (spawnChannels.isEmpty()) context.translate("modules.general.commands.set.spawn.noChannelsSet")
        else spawnChannels.joinToString(
          "\n"
        ) { "<#${it.id}> - ${it.sentMessages}/${it.requiredMessages} - ${it.spawned}" },
        context.translate("modules.general.commands.set.spawn.list.title")
      ).build()
    ).queue()
  }
}
