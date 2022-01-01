package xyz.pokecord.bot.modules.trading.commands

import xyz.pokecord.bot.core.structures.discord.base.ParentCommand

object TradeAddCommand : ParentCommand() {
  override val childCommands = mutableListOf(TradeAddCreditsCommand, TradeAddPokemonCommand)
  override val name = "Add"
}
