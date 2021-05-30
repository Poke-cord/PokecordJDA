package xyz.pokecord.bot.utils

import kotlinx.serialization.Serializable
import xyz.pokecord.bot.core.managers.I18n

@Serializable
data class PokemonStats(
  val attack: Int,
  val defense: Int,
  val hp: Int,
  val specialAttack: Int,
  val specialDefense: Int,
  val speed: Int
) {
  val total = attack + defense + hp + specialAttack + specialDefense + speed
}

@Serializable
data class FAQTranslation(
  val language: I18n.Language,
  val question: String,
  val answer: String
)

@Serializable
data class CountResult(val count: Int)

sealed class PokemonResolvable(val data: Any?) {
  class Latest : PokemonResolvable("latest")
  class Int(data: kotlin.Int?) : PokemonResolvable(data)
}
