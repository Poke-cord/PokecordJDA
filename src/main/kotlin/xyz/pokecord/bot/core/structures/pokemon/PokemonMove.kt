package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.utils.Json
import kotlin.system.exitProcess

@Serializable
data class PokemonMoveData(
  val versionGroupId: Int,
  val id: Int,
  val moveMethodId: Int,
  val requiredLevel: Int,
  val order: Int
)

@Serializable
data class PokemonMove(
  val id: Int,
  val moves: List<PokemonMoveData>
) {
  companion object {
    private val items: List<PokemonMove>

    init {
      val stream = PokemonMove::class.java.getResourceAsStream("/data/pokemon_moves.json")
      if (stream == null) {
        println("Pokemon move data not found. Exiting...")
        exitProcess(0)
      }
      val json = stream.readAllBytes().decodeToString()
      items = Json.decodeFromString(json)
    }

    fun getById(id: Int) = items.find { it.id == id }?.moves
  }
}
