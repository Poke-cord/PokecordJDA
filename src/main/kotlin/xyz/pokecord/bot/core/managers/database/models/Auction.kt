package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.litote.kmongo.Id
import xyz.pokecord.bot.utils.Config

@Serializable
data class Bid(
  val userId: String,
  val amount: Int
)

@Serializable
data class Auction(
  val id: Int,
  val ownerId: String,
  val pokemon: @Contextual Id<OwnedPokemon>,

  val timeLeft: Long,
  val startingBid: Int = Config.defaultAuctionStartingBid,
  var ended: Boolean = false,

  val bids: MutableList<Bid> = mutableListOf(),

  @Transient var _isNew: Boolean = false
) {
  val highestBid
    get() = bids.maxByOrNull { it.amount }
}