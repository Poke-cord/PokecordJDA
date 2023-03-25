package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.utils.Json
import kotlin.system.exitProcess

@Serializable
data class PokemonStat(
  val id: Int,
  val statId: Int,
  val baseStat: Int,
  val effort: Int,
) {
  companion object {
    private val items: MutableList<PokemonStat>

    init {
      val stream = PokemonStat::class.java.getResourceAsStream("/data/pokemon_stats.json")
      if (stream == null) {
        println("Pokemon stat data not found. Exiting...")
        exitProcess(0)
      }
      val json = stream.readAllBytes().decodeToString()
      items = Json.decodeFromString(json)
    }

    fun getBaseStatValue(pokemonId: Int, statId: Int) =
      items.find { it.id == pokemonId && it.statId == statId }?.baseStat

    fun getBaseEffortValue(pokemonId: Int, statId: Int) =
      items.find { it.id == pokemonId && it.statId == statId }?.effort

    fun addEntry(entry: PokemonStat) =
      items.add(entry)

    fun getByPokemonId(pokemonId: Int): List<PokemonStat> =
      items.filter { it.id == pokemonId }
  }
}
