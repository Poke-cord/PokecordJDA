package xyz.pokecord.bot.modules.staff

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

abstract class StaffCommand : Command() {
  override suspend fun canRun(context: ICommandContext): Boolean {
    return context.isStaff()
  }
}
