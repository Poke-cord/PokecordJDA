package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class TransferLog(
  val filterJson: String,
  val updateJson: String,
  val performedBy: String,
  val performedInChannel: String,
  val matchedIds: List<@Contextual Id<OwnedPokemon>> = listOf(),
  val status: Status = Status.STARTING,
  val performedInGuild: String? = null,
  @Contextual val _id: Id<TransferLog> = newId()
) {
  enum class Status {
    STARTING,
    STARTED,
    COMPLETE
  }
}
