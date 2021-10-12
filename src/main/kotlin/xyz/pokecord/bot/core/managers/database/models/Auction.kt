package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

@Serializable
data class Bid(
  val userId: String,
  val amount: Int
)

@Serializable
data class Auction(
  val ownerId: String,
  val pokemon: @Contextual Id<OwnedPokemon>,

  val endsAtTimestamp: Int,
  val startingBid: Int,
  val ended: Boolean = false,

  val bids: MutableList<Bid> = mutableListOf()
)