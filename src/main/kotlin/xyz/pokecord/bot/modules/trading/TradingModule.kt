package xyz.pokecord.bot.modules.trading

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.trading.commands.TradeCommand

class TradingModule(bot: Bot): Module(
  bot,
  arrayOf(
    TradeCommand(),
    TradeCommand.CancelCommand(),
    TradeCommand.AddCommand(),
    TradeCommand.StatusCommand(),
    TradeCommand.AddCommand.AddCredits(),
    TradeCommand.AddCommand.AddPokemon(),
  )
) {
  override val name = "Trading"
}