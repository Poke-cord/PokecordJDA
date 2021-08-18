package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

@Serializable
data class TransferLog(
  val filterJson: String,
  val updateJson: String,
  val matchedIds: List<@Contextual Id<OwnedPokemon>>,
  val performedBy: String,
  val performedInChannel: String,
  val performedInGuild: String? = null
)
