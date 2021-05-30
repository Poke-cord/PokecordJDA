package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class VoteReward(
  val userId: String,
  val season: Int,
  val claimed: Boolean = false,
  @Contextual val _id: Id<VoteReward> = newId(),
)
