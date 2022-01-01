package xyz.pokecord.bot.modules.market

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.market.commands.MarketCommand

class MarketModule(bot: Bot): Module(
  bot,
  arrayOf(MarketCommand)
) {
  override val name = "Market"
}