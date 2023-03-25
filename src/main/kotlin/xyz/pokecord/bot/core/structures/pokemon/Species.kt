package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.utils.Json
import kotlin.system.exitProcess

@Serializable
data class Species(
  val id: Int,
  val identifier: String,
  val generationId: Int,
  val evolvesFromSpeciesId: Int,
  val evolutionChainId: Int,
  val colorId: Int,
  val shapeId: Int,
  val habitatId: Int,
  val genderRate: Int,
  val captureRate: Int,
  val baseHappiness: Int,
  val isBaby: Boolean,
  val hatchCounter: Int,
  val hasGenderDifferences: Boolean,
  val growthRateId: Int,
  val formsSwitchable: Boolean,
  val isLegendary: Boolean,
  val isMythical: Boolean,
  val order: Int,
  val conquestOrder: Int,
  val romanGenerationId: String
) {
  @Serializable
  data class SpeciesName(val id: Int, val languageId: Int, val name: String, val genus: String)

  enum class Colors(val colorCode: Int) {
    BLACK(0x000000),
    BLUE(0x6890f0),
    BROWN(0x927d44),
    GRAY(0x95a5a6),
    GREEN(0x78c850),
    PINK(0xee99ac),
    PURPLE(0xa040a0),
    RED(0xe74c3c),
    WHITE(0xffffff),
    YELLOW(0xffff00),
  }

  val name = getName()
  val habitatName = getHabitatName()

  val forms by lazy {
    Pokemon.getBySpeciesId(id).mapNotNull {
      PokemonForm.getByPokemonId(it.id)
    }
  }

  fun getName(languageId: Int = 9) =
    names.find { it.id == id && it.languageId == languageId }

  fun getHabitatName(languageId: Int = 9) =
    habitatNames.find { it.id == habitatId && it.languageId == languageId }

  fun getNames() = names.filter { it.id == id }
  fun getHabitatNames() = habitatNames.filter { it.id == habitatId }

  val color = if (colorId > 0) Colors.values()[colorId - 1] else Colors.BLACK

  companion object {
    val items: List<Species>

    private val names: List<SpeciesName>
    private val habitatNames: List<Name>

    init {
      val namesStream = Species::class.java.getResourceAsStream("/data/pokemon_species_names.json")
      if (namesStream == null) {
        println("Species names not found. Exiting...")
        exitProcess(0)
      }
      val namesJson = namesStream.readAllBytes().decodeToString()
      names = Json.decodeFromString(namesJson)

      val habitatNamesStream = Species::class.java.getResourceAsStream("/data/pokemon_habitat_names.json")
      if (habitatNamesStream == null) {
        println("Habitat names not found. Exiting...")
        exitProcess(0)
      }
      val habitatNamesJson = habitatNamesStream.readAllBytes().decodeToString()
      habitatNames = Json.decodeFromString(habitatNamesJson)

      val stream = Species::class.java.getResourceAsStream("/data/pokemon_species.json")
      if (stream == null) {
        println("Species data not found. Exiting...")
        exitProcess(0)
      }
      val json = stream.readAllBytes().decodeToString()
      items = Json.decodeFromString(json)
    }

    fun getById(id: Int) = items.find { it.id == id }
  }
}
