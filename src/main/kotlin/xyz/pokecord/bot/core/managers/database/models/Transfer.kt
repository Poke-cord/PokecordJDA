package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class Transfer(
  val userId: String,
  var pokemon: MutableList<@Contextual Id<OwnedPokemon>> = mutableListOf(),

  @Contextual val _id: Id<Trade> = newId(),
  var ended: Boolean = false
)
