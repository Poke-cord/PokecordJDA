package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import xyz.pokecord.bot.utils.Config

@Serializable
data class Bid(
  val userId: String,
  val amount: Int,
  val _id: @Contextual Id<Bid> = newId()
)

@Serializable
data class Auction(
  val id: Int,
  val ownerId: String,
  val pokemon: @Contextual Id<OwnedPokemon>,

  val startingBid: Int = Config.defaultStartingBid,
  val bidIncrement: Int = Config.defaultBidIncrement,

  var timeLeft: Long = Config.defaultAuctionTime,
  var bids: MutableList<Bid> = mutableListOf(),
  var ended: Boolean = false,

  @Transient var _isNew: Boolean = false
) {
  val highestBid
    get() = bids.maxByOrNull { it.amount }
}