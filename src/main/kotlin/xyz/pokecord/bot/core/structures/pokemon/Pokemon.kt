package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.utils.Config
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
  val formName: String? = null,
  //val hasShiny: Boolean = true,
) {
  val moves by lazy {
    PokemonMove.getById(id)!!
  }
  val species
    get() = Species.getById(speciesId)!!

  val form by lazy {
    PokemonForm.getByPokemonId(id)
  }

  val leftFacing
    get() = Companion.leftFacing.contains(id)
  val rightFacing
    get() = Companion.rightFacing.contains(id)

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
    get() = "#${speciesId.toString().padStart(3, '0')}"

  fun getEmoji(isShiny: Boolean): String {
    val isEventPokemon = SpecialEvents.isEventPokemon(this)
    val emoji =
      if (isEventPokemon && isShiny) Config.Emojis.EVENT_SHINY else if (isEventPokemon) Config.Emojis.EVENT else if (isShiny) Config.Emojis.SHINY else null
    return if (emoji != null) " $emoji" else ""
  }

  companion object {
    private val nextEvolutionsCache: MutableMap<Int, List<Int>> = mutableMapOf()

    private val items: MutableList<Pokemon>
    private val types: MutableList<PokemonType>

    private var cachedMaxId: Int? = null

    val maxId: Int
      get() {
        if (cachedMaxId == null) {
          cachedMaxId = items.maxOfOrNull { it.speciesId } ?: 898
        }
        return cachedMaxId!!
        //return items.maxOfOrNull { it.speciesId } ?: 898
      }

    val leftFacing = listOf(
      1,
      3,
      4,
      5,
      6,
      7,
      8,
      10,
      11,
      12,
      13,
      14,
      15,
      16,
      17,
      19,
      20,
      21,
      22,
      23,
      24,
      25,
      26,
      27,
      29,
      30,
      31,
      32,
      33,
      34,
      37,
      42,
      43,
      44,
      45,
      46,
      47,
      49,
      50,
      52,
      54,
      55,
      56,
      57,
      58,
      59,
      61,
      62,
      63,
      65,
      67,
      69,
      70,
      71,
      72,
      73,
      75,
      76,
      77,
      78,
      79,
      80,
      81,
      84,
      85,
      86,
      87,
      88,
      90,
      91,
      92,
      93,
      95,
      97,
      98,
      99,
      101,
      102,
      103,
      104,
      105,
      107,
      108,
      110,
      111,
      112,
      113,
      115,
      116,
      117,
      119,
      120,
      121,
      124,
      125,
      126,
      127,
      128,
      129,
      130,
      131,
      132,
      133,
      135,
      136,
      137,
      139,
      140,
      141,
      142,
      143,
      144,
      145,
      148,
      149,
      150,
      153,
      154,
      157,
      158,
      159,
      160,
      161,
      162,
      164,
      165,
      166,
      167,
      168,
      170,
      171,
      172,
      174,
      175,
      176,
      177,
      178,
      179,
      180,
      181,
      182,
      185,
      186,
      187,
      188,
      189,
      190,
      191,
      192,
      193,
      195,
      196,
      198,
      199,
      202,
      203,
      204,
      205,
      206,
      207,
      208,
      214,
      215,
      216,
      217,
      218,
      219,
      220,
      221,
      223,
      224,
      225,
      226,
      227,
      228,
      230,
      231,
      232,
      234,
      235,
      236,
      238,
      239,
      240,
      243,
      244,
      245,
      246,
      248,
      249,
      251,
      252,
      253,
      254,
      255,
      256,
      258,
      260,
      261,
      262,
      263,
      264,
      266,
      267,
      270,
      272,
      273,
      274,
      275,
      276,
      277,
      278,
      279,
      280,
      281,
      283,
      284,
      285,
      286,
      287,
      288,
      290,
      291,
      292,
      294,
      295,
      296,
      297,
      298,
      301,
      302,
      303,
      304,
      305,
      306,
      307,
      308,
      309,
      310,
      311,
      312,
      313,
      314,
      315,
      316,
      318,
      319,
      320,
      321,
      322,
      323,
      324,
      325,
      326,
      327,
      328,
      329,
      330,
      331,
      332,
      333,
      334,
      335,
      336,
      337,
      338,
      339,
      340,
      341,
      342,
      343,
      344,
      345,
      346,
      347,
      348,
      349,
      350,
      351,
      352,
      353,
      354,
      355,
      356,
      358,
      360,
      361,
      362,
      363,
      365,
      366,
      367,
      368,
      369,
      370,
      371,
      372,
      373,
      374,
      375,
      377,
      379,
      380,
      381,
      382,
      384,
      386,
      387,
      388,
      389,
      390,
      391,
      392,
      394,
      395,
      396,
      397,
      398,
      399,
      401,
      402,
      404,
      405,
      406,
      407,
      410,
      411,
      412,
      413,
      414,
      415,
      416,
      417,
      418,
      419,
      420,
      422,
      423,
      424,
      425,
      426,
      427,
      428,
      429,
      430,
      431,
      432,
      433,
      434,
      435,
      436,
      437,
      438,
      439,
      440,
      441,
      442,
      443,
      444,
      445,
      447,
      448,
      449,
      450,
      451,
      452,
      453,
      454,
      455,
      456,
      457,
      458,
      459,
      460,
      461,
      462,
      463,
      464,
      465,
      466,
      468,
      469,
      470,
      472,
      473,
      474,
      475,
      476,
      478,
      480,
      481,
      484,
      485,
      486,
      487,
      489,
      490,
      492,
      493,
      494,
      495,
      497,
      499,
      500,
      501,
      502,
      503,
      504,
      505,
      506,
      507,
      508,
      509,
      510,
      511,
      512,
      513,
      514,
      517,
      518,
      519,
      520,
      521,
      523,
      524,
      525,
      526,
      527,
      528,
      529,
      533,
      534,
      535,
      536,
      537,
      539,
      540,
      541,
      542,
      543,
      544,
      545,
      546,
      547,
      548,
      549,
      550,
      551,
      553,
      554,
      555,
      557,
      558,
      559,
      560,
      561,
      563,
      564,
      565,
      566,
      567,
      568,
      569,
      570,
      571,
      572,
      573,
      574,
      576,
      577,
      578,
      579,
      580,
      581,
      582,
      583,
      584,
      585,
      586,
      587,
      588,
      589,
      590,
      591,
      592,
      594,
      595,
      596,
      597,
      598,
      600,
      601,
      602,
      603,
      604,
      606,
      608,
      609,
      610,
      611,
      612,
      613,
      614,
      615,
      616,
      618,
      619,
      620,
      621,
      622,
      623,
      624,
      625,
      626,
      627,
      628,
      629,
      631,
      632,
      633,
      634,
      635,
      636,
      637,
      638,
      639,
      641,
      644,
      646,
      647,
      648,
      649,
      650,
      651,
      652,
      653,
      654,
      655,
      656,
      657,
      659,
      661,
      662,
      663,
      664,
      665,
      666,
      667,
      669,
      671,
      672,
      673,
      674,
      675,
      676,
      677,
      679,
      680,
      681,
      682,
      684,
      685,
      687,
      689,
      690,
      691,
      692,
      693,
      694,
      695,
      697,
      698,
      699,
      700,
      701,
      703,
      704,
      706,
      707,
      709,
      710,
      711,
      712,
      713,
      714,
      715,
      717,
      718,
      719,
      720,
      721,
      722,
      724,
      725,
      726,
      727,
      730,
      731,
      732,
      733,
      734,
      736,
      737,
      738,
      739,
      740,
      742,
      743,
      745,
      746,
      747,
      748,
      749,
      751,
      753,
      754,
      755,
      756,
      757,
      759,
      761,
      762,
      764,
      765,
      766,
      768,
      770,
      771,
      773,
      774,
      775,
      776,
      778,
      779,
      780,
      781,
      782,
      783,
      784,
      786,
      787,
      790,
      792,
      793,
      794,
      795,
      796,
      799,
      800,
      801,
      802,
      803,
      804,
      805,
      807,
      809,
      811,
      812,
      813,
      816,
      817,
      818,
      822,
      824,
      826,
      828,
      829,
      830,
      833,
      834,
      836,
      837,
      839,
      840,
      841,
      842,
      843,
      846,
      847,
      848,
      851,
      852,
      853,
      855,
      856,
      857,
      858,
      859,
      860,
      861,
      865,
      866,
      867,
      868,
      869,
      870,
      871,
      874,
      875,
      879,
      880,
      881,
      882,
      883,
      884,
      885,
      886,
      887,
      889,
      890,
      891,
      892,
      894,
      896,
      897,
      898,
    )

    val rightFacing = listOf(
      2,
      9,
      18,
      28,
      35,
      36,
      38,
      39,
      40,
      41,
      48,
      51,
      53,
      60,
      64,
      66,
      68,
      74,
      82,
      83,
      89,
      94,
      96,
      100,
      106,
      109,
      114,
      118,
      122,
      123,
      134,
      138,
      146,
      147,
      151,
      152,
      155,
      156,
      163,
      169,
      173,
      183,
      184,
      194,
      197,
      200,
      201,
      209,
      210,
      211,
      212,
      213,
      222,
      229,
      233,
      237,
      241,
      242,
      247,
      250,
      257,
      259,
      265,
      268,
      269,
      271,
      282,
      289,
      293,
      299,
      300,
      317,
      357,
      359,
      364,
      376,
      378,
      383,
      385,
      393,
      400,
      403,
      408,
      409,
      421,
      446,
      467,
      471,
      477,
      479,
      482,
      483,
      488,
      491,
      496,
      498,
      515,
      516,
      522,
      530,
      531,
      532,
      538,
      552,
      556,
      562,
      575,
      593,
      599,
      605,
      607,
      617,
      630,
      640,
      642,
      643,
      645,
      658,
      660,
      668,
      670,
      678,
      683,
      686,
      688,
      696,
      702,
      705,
      708,
      716,
      723,
      728,
      729,
      735,
      741,
      744,
      750,
      752,
      758,
      760,
      763,
      767,
      769,
      772,
      777,
      785,
      788,
      789,
      791,
      797,
      798,
      806,
      808,
      810,
      814,
      815,
      819,
      820,
      821,
      823,
      825,
      827,
      831,
      832,
      835,
      838,
      844,
      845,
      849,
      850,
      854,
      862,
      863,
      864,
      872,
      873,
      876,
      878,
      888,
      893,
      895,
    )

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
      800,
      888,
      889,
      890,
      891,
      892,
      894,
      895,
      896,
      897,
      898
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
      807,
      808,
      809,
      893
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
      728,
      810,
      813,
      816
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
    val pseudoLegendaries = listOf(149, 248, 373, 376, 445, 635, 706, 784, 887)

    val dontEvolveFrom = listOf(790)
    val dontEvolveInto = listOf(292, 862, 863, 864, 865, 866, 867)

    init {
      val stream = Pokemon::class.java.getResourceAsStream("/data/pokemon.json")
      if (stream == null) {
        println("Pokemon data not found. Exiting...")
        exitProcess(0)
      }
      val json = stream.readAllBytes().decodeToString()
      items = Json.decodeFromString<List<Pokemon>>(json).filter { it.id <= 898 }.toMutableList()
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
      } || it.form?.names?.any { formName ->
        formName.pokemonName.equals(targetName, true)
      } ?: false
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
      "https://images.pokecord.zihad.dev/${if (shiny) "shiny" else "regular"}/${id}.png"

    fun getBySpeciesId(speciesId: Int): List<Pokemon> =
      items.filter { it.speciesId == speciesId }

    fun addEntry(entry: Pokemon) = items.add(entry)
    fun addTypeEntry(entry: PokemonType) = types.add(entry)
  }
}
