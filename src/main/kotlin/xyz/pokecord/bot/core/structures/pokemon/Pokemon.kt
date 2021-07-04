package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.utils.Json
import kotlin.system.exitProcess

@Serializable
data class Pokemon(
  val id: Int,
  val identifier: String,
  val name: String,
  val speciesId: Int,
  val height: Int,
  val weight: Int,
  val baseExp: Int?,
  val order: Int?,
  val isDefault: Boolean,
  val formName: String? = null
) {
  val moves by lazy {
    PokemonMove.getById(id)!!
  }
  val species
    get() = Species.getById(speciesId)!!

  val types by lazy {
    Companion.types.find { it.id == this.id }?.types?.map { Type.getById(it)!! }!!
  }

  val nextEvolutions by lazy {
    if (nextEvolutionsCache.contains(this.id)) return@lazy nextEvolutionsCache[this.id]!!
    val nextEvolutions = mutableListOf<Int>()
    val evolutionChain = PokemonEvolution.items.filter {
      EvolutionChain.of(id)?.evolvedSpeciesIds?.contains(it.evolvedSpeciesId) == true
    }
    for (evolution in evolutionChain) {
      val lastEvolutionSpecies = Species.items.find {
        it.id == evolution.evolvedSpeciesId
      }
      if (lastEvolutionSpecies?.evolvesFromSpeciesId == this.id && !nextEvolutions.contains(evolution.evolvedSpeciesId)) {
        nextEvolutions.add(evolution.evolvedSpeciesId)
      }
    }
    nextEvolutions.sort()
    nextEvolutionsCache[this.id] = nextEvolutions
    return@lazy nextEvolutions
  }

  val imageUrl by lazy {
    getImageUrl(id)
  }

  val formattedSpeciesId
    get() = "#${id.toString().padStart(3, '0')}"

  companion object {
    private val nextEvolutionsCache: MutableMap<Int, List<Int>> = mutableMapOf()

    private val items: List<Pokemon>
    private val types: List<PokemonType>

    private var cachedMaxId: Int? = null

    val maxId: Int
      get() {
        if (cachedMaxId == null) {
          cachedMaxId = items.maxOfOrNull { it.id } ?: 807
        }
        return cachedMaxId!!
      }

    val legendaries = listOf(
      144,
      145,
      146,
      150,
      243,
      244,
      245,
      249,
      250,
      377,
      378,
      379,
      380,
      381,
      382,
      383,
      384,
      480,
      481,
      482,
      483,
      484,
      485,
      486,
      487,
      488,
      638,
      639,
      640,
      641,
      642,
      643,
      644,
      645,
      646,
      716,
      717,
      718,
      772,
      773,
      785,
      786,
      787,
      788,
      789,
      790,
      791,
      792,
      800
    )
    val mythicals = listOf(
      151,
      251,
      385,
      386,
      489,
      490,
      491,
      492,
      493,
      494,
      647,
      648,
      649,
      719,
      720,
      721,
      801,
      802,
      807
    )
    val starters = listOf(
      1,
      4,
      7,
      152,
      155,
      158,
      252,
      255,
      258,
      387,
      390,
      393,
      495,
      498,
      501,
      650,
      653,
      656,
      722,
      725,
      728
    )
    val ultraBeasts = listOf(
      793,
      794,
      795,
      796,
      797,
      798,
      799,
      803,
      804,
      805,
      806,
    )
    val pseudoLegendaries = listOf(149, 248, 373, 376, 445, 635, 706, 784)

    init {
      val stream = Pokemon::class.java.getResourceAsStream("/data/pokemon.json")
      if (stream == null) {
        println("Pokemon data not found. Exiting...")
        exitProcess(0)
      }
      val json = stream.readAllBytes().decodeToString()
      items = Json.decodeFromString<List<Pokemon>>(json).filter { it.id <= 807 }
      val typesStream = Pokemon::class.java.getResourceAsStream("/data/pokemon_types.json")
      if (typesStream == null) {
        println("Pokemon types data not found. Exiting...")
        exitProcess(0)
      }
      val typesJson = typesStream.readAllBytes().decodeToString()
      types = Json.decodeFromString(typesJson)
    }

    fun getById(id: Int) = items.find { it.id == id }

    fun getByTypes(types: List<Type>): List<Pokemon> {
      return items.filter { types.any { type -> it.types.contains(type) } }
    }

    fun getByName(targetName: String) = items.find {
      val identifierMatch =
        it.identifier.equals(targetName, true) ||
            it.identifier.replace('-', ' ').equals(targetName, true) ||
            it.identifier.replace("-", "").equals(targetName, true)

      identifierMatch || it.species.getNames().any { name ->
        name.name.equals(targetName, true)
      }
    }

    fun searchRegex(regex: Regex) = items.filter { pokemon ->
      val identifierMatch =
        regex.containsMatchIn(pokemon.identifier) ||
            regex.containsMatchIn(pokemon.identifier.replace('-', ' ')) ||
            regex.containsMatchIn(pokemon.identifier.replace("-", ""))

      identifierMatch || pokemon.species.getNames().any { speciesName ->
        regex.containsMatchIn(speciesName.name)
      }
    }

    fun search(query: String) = items.filter { pokemon ->
      val identifierMatch =
        pokemon.identifier.contains(query, true) ||
            pokemon.identifier.replace('-', ' ').contains(query, true) ||
            pokemon.identifier.replace("-", "").contains(query, true)

      identifierMatch || pokemon.species.getNames().any { speciesName ->
        speciesName.name.contains(query, true)
      }
    }

    fun getImageUrl(id: Int, shiny: Boolean = false) =
//      "https://pokecord-images.s3.wasabisys.com/${if (shiny) "shiny" else "regular"}/${id}.png"
      "https://images.pokecord.xyz/${if (shiny) "shiny" else "regular"}/${id}.png"
  }
}
