package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class Order(
  val orderId: String,
  val packageId: String,
  val itemId: String,
  val price: Double,
  val userId: String,
  val userName: String,
  val paid: Boolean = false,
  val payeeEmail: String? = null,
  @Contextual val _id: Id<Order> = newId()
)
