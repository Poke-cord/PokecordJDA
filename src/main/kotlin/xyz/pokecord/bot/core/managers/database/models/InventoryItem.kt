package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class InventoryItem(
  val id: Int,
  val ownerId: String,
  var amount: Int = 0,
  @Contextual val _id: Id<InventoryItem> = newId(),
)
