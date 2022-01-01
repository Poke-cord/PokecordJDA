package xyz.pokecord.bot.modules.trading

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.trading.commands.TradeCommand

class TradingModule(bot: Bot) : Module(
  bot,
  arrayOf(TradeCommand)
) {
  override val name = "Trading"
}