package xyz.pokecord.bot.modules.staff.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.modules.staff.StaffCommand

class ShowCommand : StaffCommand() {
  override val name = "Show"

  @Executor
  suspend fun execute(context: ICommandContext) {
    if (!context.isFromGuild) return
    val spawnChannels = module.bot.database.spawnChannelRepository.getSpawnChannels(context.guild!!.id)

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
