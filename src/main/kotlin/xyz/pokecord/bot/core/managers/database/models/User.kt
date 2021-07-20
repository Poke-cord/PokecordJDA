package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import xyz.pokecord.bot.core.managers.I18n

@Serializable
data class User(
  val id: String,
  var tag: String? = null,
  val blacklisted: Boolean = false,
  val caughtPokemon: MutableList<Int> = mutableListOf(),
  val caughtShinies: MutableList<Int> = mutableListOf(),
  var credits: Int = 1000,
  var gems: Int = 0,
  var tokens: Int = 0,
  val releasedPokemon: MutableList<Int> = mutableListOf(),
  val releasedShinies: MutableList<Int> = mutableListOf(),
  var progressPrivate: Boolean = false,
  var donationTier: Int = 0,
  @Contextual var selected: Id<OwnedPokemon>? = null,
  var shinyRate: Double = 4908.0,
  var agreedToTerms: Boolean = false,
  val language: I18n.Language? = null,
  var pokemonCount: Int = 0,
  var nextIndex: Int = 0,
  var lastVoteAt: Long? = null,
  @Contextual val _id: Id<User> = newId(),
  @Transient var _isNew: Boolean = false
) {
  fun getShopDiscount(): Double {
    return when (donationTier) {
      6 -> 0.75
      5 -> 0.8
      4 -> 0.85
      3 -> 0.9
      else -> 1.0
    }
  }

  val isDefault
    get() = !agreedToTerms
        && language == null
        && !blacklisted
        && caughtPokemon.isEmpty()
        && caughtShinies.isEmpty()
        && credits == 1000
        && gems == 0
        && releasedPokemon.isEmpty()
        && releasedShinies.isEmpty()
        && !progressPrivate
        && donationTier == 0
        && pokemonCount == 0
        && nextIndex == 0
        && shinyRate == 4908.0
}
