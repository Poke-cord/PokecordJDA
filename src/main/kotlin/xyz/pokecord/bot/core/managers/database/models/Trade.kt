package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class TraderData(
  val userId: String,
  val partnerId: String,
  var confirmed: Boolean = false,
  var credits: Int = 0,
  var pokemon: MutableList<Int> = mutableListOf()
)

@Serializable
data class Trade(
  val initiator: TraderData,
  val receiver: TraderData,

  @Contextual val _id: Id<Guild> = newId(),
)