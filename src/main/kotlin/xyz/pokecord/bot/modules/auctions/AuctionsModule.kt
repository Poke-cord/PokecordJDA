package xyz.pokecord.bot.modules.auctions

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.auctions.commands.AuctionsCommand

class AuctionsModule(bot: Bot): Module(
  bot,
  arrayOf(
    AuctionsCommand
  )
) {
  override val name = "Auctions"
}