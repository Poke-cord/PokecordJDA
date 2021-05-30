package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.utils.Json
import kotlin.system.exitProcess

@Serializable
data class Moveset(
  val id: Int,
  val moves: List<Int>
) {
  companion object {
    private val items: List<Moveset>

    init {
      val stream = Moveset::class.java.getResourceAsStream("/data/movesets.json")
      if (stream == null) {
        println("Moveset data not found. Exiting...")
        exitProcess(0)
      }
      val json = stream.readAllBytes().decodeToString()
      items = Json.decodeFromString(json)
    }

    fun getByPokemonId(pokemonId: Int) = items.find { it.id == pokemonId }
  }
}
