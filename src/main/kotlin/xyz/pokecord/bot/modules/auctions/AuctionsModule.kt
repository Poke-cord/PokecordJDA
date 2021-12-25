package xyz.pokecord.bot.modules.auctions

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.auctions.commands.AuctionsCommand
import xyz.pokecord.bot.modules.auctions.tasks.AuctionTask

class AuctionsModule(bot: Bot): Module(
  bot,
  arrayOf(
    AuctionsCommand
  ),
  arrayOf(),
  arrayOf(
    AuctionTask
  )
) {
  override val name = "Auctions"
}