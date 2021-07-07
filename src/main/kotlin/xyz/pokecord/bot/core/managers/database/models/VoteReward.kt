package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class VoteReward(
  val userId: String,
  val credits: Int,
  val tokens: Int,
  val cct: Int = 0,
  @Contextual val _id: Id<VoteReward> = newId(),
)
