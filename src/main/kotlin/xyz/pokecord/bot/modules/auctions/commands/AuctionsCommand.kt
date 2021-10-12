package xyz.pokecord.bot.modules.auctions.commands

import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.discord.base.ParentCommand

object AuctionsCommand: ParentCommand() {
  override val name = "Auctions"
  override var aliases = arrayOf("ah", "auction")
  override val childCommands: MutableList<Command> = mutableListOf(ListCommand)
}