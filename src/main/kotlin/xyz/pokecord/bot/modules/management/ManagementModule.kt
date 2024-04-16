package xyz.pokecord.bot.modules.management

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.management.commands.SetCommand

class ManagementModule(bot: Bot): Module(
  bot,
  arrayOf(SetCommand())
) {
  override val name = "Server Management"
}