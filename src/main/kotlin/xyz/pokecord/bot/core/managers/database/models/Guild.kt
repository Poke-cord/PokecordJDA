package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.pokecord.bot.core.managers.I18n

@Serializable
data class Guild(
  val id: String,
  var prefix: String? = null,
  val levelUpMessagesSilenced: Boolean = false,
  val language: I18n.Language? = null
)
