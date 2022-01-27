package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.litote.kmongo.Id

@Serializable
data class Listing(
  val id: Int,
  val ownerId: String,
  val pokemon: @Contextual Id<OwnedPokemon>,

  val price: Int = 1000,
  var sold: Boolean = false,
  var soldTo: String? = null,
  var unlisted: Boolean = false,

  @Transient var _isNew: Boolean = false
)