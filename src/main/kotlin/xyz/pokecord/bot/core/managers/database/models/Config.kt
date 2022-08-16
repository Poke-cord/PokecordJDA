package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class PaypalCredentials(
  val accessToken: String,
  @Contextual val endsAt: Date
)

@Serializable
data class Config(
  val paypalCredentials: PaypalCredentials? = null,
  val susBlacklistIds: List<String> = emptyList()
)
