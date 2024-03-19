package xyz.pokecord.bot.modules.trading.commands

import xyz.pokecord.bot.core.structures.discord.base.ParentCommand

object TradeRemoveCommand : ParentCommand() {
  override val childCommands = mutableListOf(TradeRemovePokemonCommand, TradeRemoveCreditsCommand)
  override val name = "Remove"
  override var aliases = arrayOf("r", "rm")
}