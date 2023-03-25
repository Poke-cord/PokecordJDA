package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.utils.Json
import kotlin.system.exitProcess

@Serializable
data class PokemonForm(
  val id: Int,
  val identifier: String,
  val formIdentifier: String,
  val pokemonId: Int,
  val introducedInVersionGroupId: Int,
  val isDefault: Boolean,
  val isBattleOnly: Boolean,
  val isMega: Boolean,
  val formOrder: Int,
  val order: Int,
) {
  val names by lazy {
    PokemonFormName.getById(id)
  }

  companion object {
    fun getByPokemonId(pokemonId: Int): PokemonForm? {
      return items.find {
        it.pokemonId == pokemonId
      }
    }

    private val items: MutableList<PokemonForm>

    fun addEntry(entry: PokemonForm) =
      items.add(entry)

    init {
      val stream = PokemonForm::class.java.getResourceAsStream("/data/pokemon_forms.json")
      if (stream == null) {
        println("Pokemon forms data not found. Exiting...")
        exitProcess(0)
      }
      val json = stream.readAllBytes().decodeToString()
      items = Json.decodeFromString(json)
    }
  }
}