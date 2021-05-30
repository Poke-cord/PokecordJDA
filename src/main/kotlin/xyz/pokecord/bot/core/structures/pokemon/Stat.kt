package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.utils.Json
import kotlin.system.exitProcess

@Serializable
data class Stat(
  val id: Int,
  val damageClassId: Int,
  val identifier: String,
  val isBattleOnly: Boolean,
  val gameIndex: Int
) {
  fun getBaseValue(pokemonId: Int) = PokemonStat.getBaseStatValue(pokemonId, id)
  fun getBaseEffortValue(pokemonId: Int) = PokemonStat.getBaseEffortValue(pokemonId, id)
  fun getName(languageId: Int = 9) = names.find { it.id == id && it.languageId == languageId }

  val name = getName()

  companion object {
    private var items: List<Stat> = listOf()
    private val names: List<Name>


    val hp by lazy {
      items.find { it.identifier == "hp" }!!
    }
    val attack by lazy {
      items.find { it.identifier == "attack" }!!
    }
    val defense by lazy {
      items.find { it.identifier == "defense" }!!
    }
    val specialAttack by lazy {
      items.find { it.identifier == "special-attack" }!!
    }
    val specialDefense by lazy {
      items.find { it.identifier == "special-defense" }!!
    }
    val speed by lazy {
      items.find { it.identifier == "speed" }!!
    }

    init {
      val namesStream = Stat::class.java.getResourceAsStream("/data/stat_names.json")
      if (namesStream == null) {
        println("Stat names not found. Exiting...")
        exitProcess(0)
      }
      val namesJson = namesStream.readAllBytes().decodeToString()
      names = Json.decodeFromString(namesJson)

      val stream = Stat::class.java.getResourceAsStream("/data/stats.json")
      if (stream == null) {
        println("Stat data not found. Exiting...")
        exitProcess(0)
      }
      val json = stream.readAllBytes().decodeToString()
      items = Json.decodeFromString(json)
    }
  }
}
