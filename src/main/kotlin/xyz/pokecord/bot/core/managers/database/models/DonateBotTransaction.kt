package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import xyz.pokecord.bot.utils.Json

@Serializable
data class DonateBotTransaction(
  @SerialName("txn_id")
  val id: String,
  val status: Status,
  val price: String,
  val currency: String,
  @SerialName("buyer_email")
  val buyerEmail: String,
  @SerialName("seller_email")
  val sellerEmail: String,
  val recurring: Int,
  @SerialName("guild_id")
  val guildId: String,
  @SerialName("seller_customs")
  val sellerCustoms: JsonElement? = JsonNull,
  @SerialName("role_id")
  val roleId: String? = null,
  @SerialName("product_id")
  val productId: String? = null,
  @SerialName("raw_buyer_id")
  val rawBuyerId: String? = null,
) {
  @Serializable
  enum class Status {
    @SerialName("Completed")
    COMPLETED,

    @SerialName("Reversed")
    REVERSED,

    @SerialName("Refunded")
    REFUNDED,

    @SerialName("sub_ended")
    SUB_ENDED;

    override fun toString(): String {
      val json = Json.encodeToString(this)
      return json.removeSurrounding("\"")
    }
  }
}
