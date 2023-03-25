package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.utils.Json
import kotlin.system.exitProcess

@Serializable
data class PokemonFormName(
  val id: Int,
  val languageId: Int,
  val formName: String,
  val pokemonName: String,
) {
  companion object {
    fun getById(id: Int): List<PokemonFormName> {
      return items.filter {
        it.id == id
      }
    }

    fun addEntry(entry: PokemonFormName) =
      items.add(entry)

    private val items: MutableList<PokemonFormName>

    init {
      val stream = PokemonFormName::class.java.getResourceAsStream("/data/pokemon_form_names.json")
      if (stream == null) {
        println("Pokemon form names data not found. Exiting...")
        exitProcess(0)
      }
      val json = stream.readAllBytes().decodeToString()

      items = Json.decodeFromString(json)
    }
  }
}
