package xyz.pokecord.bot.modules.developer

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.developer.commands.*
import xyz.pokecord.bot.modules.developer.tasks.RedisSyncTask
import xyz.pokecord.bot.modules.developer.tasks.StaffSyncTask

class DeveloperModule(bot: Bot) : Module(
  bot,
  arrayOf(
    EvalCommand(),
    ShowCommand(),
    ParseEntitiesCommand(),
    MockCommand(),
    MaintenanceCommand(),
    ShardsCommand()
  ),
  tasks = arrayOf(
    RedisSyncTask(), StaffSyncTask()
  )
) {
  override val name = "Developer"
}
