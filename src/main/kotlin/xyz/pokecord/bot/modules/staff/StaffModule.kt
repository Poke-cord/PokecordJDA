package xyz.pokecord.bot.modules.staff

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.staff.tasks.StaffSyncTask
import xyz.pokecord.bot.modules.staff.commands.MaintenanceCommand
import xyz.pokecord.bot.modules.staff.commands.ShardsCommand

class StaffModule(bot: Bot) : Module(
  bot,
  arrayOf(
    MaintenanceCommand(),
    ShardsCommand()
  ),
  tasks = arrayOf(
    StaffSyncTask()
  )
) {
  override val name = "Staff"
}
