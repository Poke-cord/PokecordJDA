package xyz.pokecord.bot.modules.staff

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.staff.commands.BlacklistCommand
import xyz.pokecord.bot.modules.staff.commands.MaintenanceCommand
import xyz.pokecord.bot.modules.staff.commands.ShardsCommand
import xyz.pokecord.bot.modules.staff.tasks.StaffSyncTask

class StaffModule(bot: Bot) : Module(
  bot,
  arrayOf(
    MaintenanceCommand(),
    ShardsCommand(),
    BlacklistCommand()
  ),
  tasks = arrayOf(
    StaffSyncTask()
  )
) {
  override val name = "Staff"
}
