package xyz.pokecord.bot.modules.profile.commands

import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.modules.profile.commands.item.UseItemCommand
import xyz.pokecord.bot.modules.profile.commands.item.GiveItemCommand
import xyz.pokecord.bot.modules.profile.commands.item.TakeItemCommand

object ItemCommand : ParentCommand() {
  override val childCommands = mutableListOf(GiveItemCommand, TakeItemCommand, UseItemCommand)
  override val name = "Item"
}
