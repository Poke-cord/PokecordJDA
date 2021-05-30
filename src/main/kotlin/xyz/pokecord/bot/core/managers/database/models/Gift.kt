package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Serializable
import org.litote.kmongo.Id

@Serializable
data class Gift(
  val senderId: String,
  val receiverId: String,
  val credits: Int = 0,
  val pokemonIds: MutableList<Id<OwnedPokemon>>
)
