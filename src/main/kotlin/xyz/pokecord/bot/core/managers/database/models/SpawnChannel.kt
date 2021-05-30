package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class SpawnChannel(
  val id: String,
  val guildId: String,
  var requiredMessages: Int,
  var sentMessages: Int,
  var spawned: Int,
  @Contextual val _id: Id<SpawnChannel> = newId()
)
