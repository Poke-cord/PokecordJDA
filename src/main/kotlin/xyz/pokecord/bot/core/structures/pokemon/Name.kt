package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable

@Serializable
data class Name(
  val id: Int,
  val languageId: Int,
  val name: String
)
