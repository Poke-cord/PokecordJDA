package xyz.pokecord.bot.modules.trades

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.trades.commands.TradeCommand

class TradesModule(bot: Bot) : Module(
  bot,
  arrayOf(
    TradeCommand(),
    TradeCommand.TradeStatusCommand()
  )
) {
  override val name = "Trades"
}
