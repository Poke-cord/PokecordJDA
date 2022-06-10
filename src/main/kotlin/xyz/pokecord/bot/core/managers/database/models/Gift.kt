package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class Gift(
  val senderId: String,
  val receiverId: String,
  val credits: Int = 0,
  val pokemonIds: List<@Contextual Id<OwnedPokemon>>,
  val _id: @Contextual Id<Gift> = newId()
)
