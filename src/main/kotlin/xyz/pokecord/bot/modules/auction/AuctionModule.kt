package xyz.pokecord.bot.modules.auction

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.auction.commands.AuctionCommand
import xyz.pokecord.bot.modules.auction.tasks.AuctionTask

class AuctionModule(bot: Bot): Module(
  bot,
  arrayOf(AuctionCommand),
  arrayOf(),
  arrayOf(AuctionTask)
) {
  override val name = "Auction"
}