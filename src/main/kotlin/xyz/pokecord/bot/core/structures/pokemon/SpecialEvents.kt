package xyz.pokecord.bot.core.structures.pokemon

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object SpecialEvents {
  private val catchableEventList by lazy {
    listOf(
      EventPokemon( //pride 2023
        "Pride 2023",
        parseDateTime("2023-06-30 00:00:00"),
        parseDateTime("2023-07-31 00:00:00"),
        mapOf(
          Pokemon.getByName("Charizard")!!.species to listOf(
            Pokemon.getByName("Pride Charizard V1")!!,
            Pokemon.getByName("Pride Charizard V2")!!
          ),
          Pokemon.getByName("Pidgey")!!.species to listOf(
            Pokemon.getByName("Pride Pidgey V1")!!,
            Pokemon.getByName("Pride Pidgey V2")!!
          ),
          Pokemon.getByName("Pikachu")!!.species to listOf(
            Pokemon.getByName("Pride Pikachu V1")!!,
            Pokemon.getByName("Pride Pikachu V2")!!
          ),
          Pokemon.getById(29)!!.species to listOf(
            Pokemon.getByName("Pride Nidoran♀ V1")!!,
            Pokemon.getByName("Pride Nidoran♀ V2")!!
          ),
          Pokemon.getByName("Venomoth")!!.species to listOf(
            Pokemon.getByName("Pride Venomoth V1")!!,
            Pokemon.getByName("Pride Venomoth V2")!!
          ),
          Pokemon.getByName("Mankey")!!.species to listOf(
            Pokemon.getByName("Pride Mankey V1")!!,
            Pokemon.getByName("Pride Mankey V2")!!
          ),
          Pokemon.getByName("Chansey")!!.species to listOf(
            Pokemon.getByName("Pride Chansey V1")!!,
            Pokemon.getByName("Pride Chansey V2")!!
          ),
          Pokemon.getByName("Eevee")!!.species to listOf(
            Pokemon.getByName("Pride Eevee V1")!!,
            Pokemon.getByName("Pride Eevee V2")!!
          ),
          Pokemon.getByName("Mewtwo")!!.species to listOf(
            Pokemon.getByName("Pride Mewtwo V1")!!,
            Pokemon.getByName("Pride Mewtwo V2")!!
          ),
          Pokemon.getByName("Umbreon")!!.species to listOf(
            Pokemon.getByName("Pride Umbreon V1")!!,
            Pokemon.getByName("Pride Umbreon V2")!!
          ),
          Pokemon.getByName("Linoone")!!.species to listOf(
            Pokemon.getByName("Pride Linoone V1")!!,
            Pokemon.getByName("Pride Linoone V2")!!
          ),
          Pokemon.getByName("Wingull")!!.species to listOf(
            Pokemon.getByName("Pride Wingull V1")!!,
            Pokemon.getByName("Pride Wingull V2")!!
          ),
          Pokemon.getByName("Minun")!!.species to listOf(
            Pokemon.getByName("Pride Minun V1")!!,
            Pokemon.getByName("Pride Minun V2")!!
          ),
          Pokemon.getByName("Wailord")!!.species to listOf(
            Pokemon.getByName("Pride Wailord V1")!!,
            Pokemon.getByName("Pride Wailord V2")!!
          ),
          Pokemon.getByName("Lunatone")!!.species to listOf(
            Pokemon.getByName("Pride Lunatone V1")!!,
            Pokemon.getByName("Pride Lunatone V2")!!
          ),
          Pokemon.getByName("Solrock")!!.species to listOf(
            Pokemon.getByName("Pride Solrock V1")!!,
            Pokemon.getByName("Pride Solrock V2")!!
          ),
          Pokemon.getByName("Milotic")!!.species to listOf(
            Pokemon.getByName("Pride Milotic V1")!!,
            Pokemon.getByName("Pride Milotic V2")!!
          ),
          Pokemon.getByName("Rayquaza")!!.species to listOf(
            Pokemon.getByName("Pride Rayquaza V1")!!,
            Pokemon.getByName("Pride Rayquaza V2")!!
          ),
          Pokemon.getByName("Prinplup")!!.species to listOf(
            Pokemon.getByName("Pride Prinplup V1")!!,
            Pokemon.getByName("Pride Prinplup V2")!!
          ),
          Pokemon.getByName("Wormadam")!!.species to listOf(
            Pokemon.getByName("Pride Wormadam V1")!!,
            Pokemon.getByName("Pride Wormadam V2")!!
          ),
          Pokemon.getByName("Garchomp")!!.species to listOf(
            Pokemon.getByName("Pride Garchomp V1")!!,
            Pokemon.getByName("Pride Garchomp V2")!!
          ),
          Pokemon.getByName("Lucario")!!.species to listOf(
            Pokemon.getByName("Pride Lucario V1")!!,
            Pokemon.getByName("Pride Lucario V2")!!
          ),
          Pokemon.getByName("Darkrai")!!.species to listOf(
            Pokemon.getByName("Pride Darkrai V1")!!,
            Pokemon.getByName("Pride Darkrai V2")!!
          ),
          Pokemon.getByName("Arceus")!!.species to listOf(
            Pokemon.getByName("Pride Arceus V1")!!,
            Pokemon.getByName("Pride Arceus V2")!!
          ),
          Pokemon.getByName("Serperior")!!.species to listOf(
            Pokemon.getByName("Pride Serperior V1")!!,
            Pokemon.getByName("Pride Serperior V2")!!
          ),
          Pokemon.getByName("Dewott")!!.species to listOf(
            Pokemon.getByName("Pride Dewott V1")!!,
            Pokemon.getByName("Pride Dewott V2")!!
          ),
          Pokemon.getByName("Greninja")!!.species to listOf(
            Pokemon.getByName("Pride Greninja V1")!!,
            Pokemon.getByName("Pride Greninja V2")!!
          ),
          Pokemon.getByName("Trevenant")!!.species to listOf(
            Pokemon.getByName("Pride Trevenant V1")!!,
            Pokemon.getByName("Pride Trevenant V2")!!
          ),
          Pokemon.getByName("Bergmite")!!.species to listOf(
            Pokemon.getByName("Pride Bergmite V1")!!,
            Pokemon.getByName("Pride Bergmite V2")!!
          ),
          Pokemon.getByName("Yveltal")!!.species to listOf(
            Pokemon.getByName("Pride Yveltal V1")!!,
            Pokemon.getByName("Pride Yveltal V2")!!
          ),
          Pokemon.getByName("Togedemaru")!!.species to listOf(
            Pokemon.getByName("Pride Togedemaru V1")!!,
            Pokemon.getByName("Pride Togedemaru V2")!!
          ),
          Pokemon.getByName("Rookidee")!!.species to listOf(
            Pokemon.getByName("Pride Rookidee V1")!!,
            Pokemon.getByName("Pride Rookidee V2")!!
          ),
          Pokemon.getByName("Boltund")!!.species to listOf(
            Pokemon.getByName("Pride Boltund V1")!!,
            Pokemon.getByName("Pride Boltund V2")!!
          ),
          Pokemon.getByName("Runerigus")!!.species to listOf(
            Pokemon.getByName("Pride Runerigus V1")!!,
            Pokemon.getByName("Pride Runerigus V2")!!
          ),
          Pokemon.getByName("Arctovish")!!.species to listOf(
            Pokemon.getByName("Pride Arctovish V1")!!,
            Pokemon.getByName("Pride Arctovish V2")!!
          ),
        )
      ),
      EventPokemon( //mayflowers 2024 week 1 - awareness
        "Mayflowers 2024",
        parseDateTime("2024-05-06 07:00:00"),
        parseDateTime("2024-05-13 06:59:59"),
        mapOf(
          Pokemon.getByName("Wartortle")!!.species to listOf(
            Pokemon.getByName("Awareness Wartortle V1")!!,
            Pokemon.getByName("Awareness Wartortle V2")!!
          ),
          Pokemon.getByName("Pidgeotto")!!.species to listOf(
            Pokemon.getByName("Awareness Pidgeotto V1")!!,
            Pokemon.getByName("Awareness Pidgeotto V2")!!
          ),
          Pokemon.getByName("Grimer")!!.species to listOf(
            Pokemon.getByName("Awareness Grimer V1")!!,
            Pokemon.getByName("Awareness Grimer V2")!!
          ),
          Pokemon.getByName("Kingler")!!.species to listOf(
            Pokemon.getByName("Awareness Kingler V1")!!,
            Pokemon.getByName("Awareness Kingler V2")!!
          ),
          Pokemon.getByName("Elekid")!!.species to listOf(
            Pokemon.getByName("Awareness Elekid V1")!!,
            Pokemon.getByName("Awareness Elekid V2")!!
          ),
          Pokemon.getByName("Trapinch")!!.species to listOf(
            Pokemon.getByName("Awareness Trapinch V1")!!,
            Pokemon.getByName("Awareness Trapinch V2")!!
          ),
          Pokemon.getByName("Glalie")!!.species to listOf(
            Pokemon.getByName("Awareness Glalie V1")!!,
            Pokemon.getByName("Awareness Glalie V2")!!
          ),
          Pokemon.getByName("Staraptor")!!.species to listOf(
            Pokemon.getByName("Awareness Staraptor V1")!!,
            Pokemon.getByName("Awareness Staraptor V2")!!
          ),
          Pokemon.getByName("Kricketot")!!.species to listOf(
            Pokemon.getByName("Awareness Kricketot V1")!!,
            Pokemon.getByName("Awareness Kricketot V2")!!
          ),
          Pokemon.getByName("Gible")!!.species to listOf(
            Pokemon.getByName("Awareness Gible V1")!!,
            Pokemon.getByName("Awareness Gible V2")!!
          ),
          Pokemon.getByName("Krookodile")!!.species to listOf(
            Pokemon.getByName("Awareness Krookodile V1")!!,
            Pokemon.getByName("Awareness Krookodile V2")!!
          ),
          Pokemon.getByName("Zorua")!!.species to listOf(
            Pokemon.getByName("Awareness Zorua V1")!!,
            Pokemon.getByName("Awareness Zorua V2")!!
          ),
          Pokemon.getByName("Mienfoo")!!.species to listOf(
            Pokemon.getByName("Awareness Mienfoo V1")!!,
            Pokemon.getByName("Awareness Mienfoo V2")!!
          ),
          Pokemon.getByName("Thundurus")!!.species to listOf(
            Pokemon.getByName("Awareness Thundurus V1")!!,
            Pokemon.getByName("Awareness Thundurus V2")!!
          ),
          Pokemon.getByName("Swirlix")!!.species to listOf(
            Pokemon.getByName("Awareness Swirlix V1")!!,
            Pokemon.getByName("Awareness Swirlix V2")!!
          ),
          Pokemon.getByName("Clawitzer")!!.species to listOf(
            Pokemon.getByName("Awareness Clawitzer V1")!!,
            Pokemon.getByName("Awareness Clawitzer V2")!!
          ),
          Pokemon.getByName("Diancie")!!.species to listOf(
            Pokemon.getByName("Awareness Diancie V1")!!,
            Pokemon.getByName("Awareness Diancie V2")!!
          ),
          Pokemon.getByName("Rowlet")!!.species to listOf(
            Pokemon.getByName("Awareness Rowlet V1")!!,
            Pokemon.getByName("Awareness Rowlet V2")!!
          ),
          Pokemon.getByName("Toxapex")!!.species to listOf(
            Pokemon.getByName("Awareness Toxapex V1")!!,
            Pokemon.getByName("Awareness Toxapex V2")!!
          ),
          Pokemon.getByName("Dhelmise")!!.species to listOf(
            Pokemon.getByName("Awareness Dhelmise V1")!!,
            Pokemon.getByName("Awareness Dhelmise V2")!!
          ),
          Pokemon.getByName("Cramorant")!!.species to listOf(
            Pokemon.getByName("Awareness Cramorant V1")!!,
            Pokemon.getByName("Awareness Cramorant V2")!!
          ),
          Pokemon.getById(866)!!.species to listOf(
            Pokemon.getByName("Awareness Mr. Rime V1")!!,
            Pokemon.getByName("Awareness Mr. Rime V2")!!
          ),
        )
      ),
      /*
      EventPokemon( //mayflowers 2024 week 2 - mother's day
        "Mayflowers 2024",
        parseDateTime("2024-05-13 07:00:00"),
        parseDateTime("2024-05-20 06:59:59"),
        mapOf(
          Pokemon.getByName("Wartortle")!!.species to listOf(
            Pokemon.getByName("Awareness Wartortle V1")!!,
            Pokemon.getByName("Awareness Wartortle V2")!!
          ),
          Pokemon.getByName("Pidgeotto")!!.species to listOf(
            Pokemon.getByName("Awareness Pidgeotto V1")!!,
            Pokemon.getByName("Awareness Pidgeotto V2")!!
          ),
          Pokemon.getByName("Grimer")!!.species to listOf(
            Pokemon.getByName("Awareness Grimer V1")!!,
            Pokemon.getByName("Awareness Grimer V2")!!
          ),
          Pokemon.getByName("Kingler")!!.species to listOf(
            Pokemon.getByName("Awareness Kingler V1")!!,
            Pokemon.getByName("Awareness Kingler V2")!!
          ),
          Pokemon.getByName("Elekid")!!.species to listOf(
            Pokemon.getByName("Awareness Elekid V1")!!,
            Pokemon.getByName("Awareness Elekid V2")!!
          ),
          Pokemon.getByName("Trapinch")!!.species to listOf(
            Pokemon.getByName("Awareness Trapinch V1")!!,
            Pokemon.getByName("Awareness Trapinch V2")!!
          ),
          Pokemon.getByName("Glalie")!!.species to listOf(
            Pokemon.getByName("Awareness Glalie V1")!!,
            Pokemon.getByName("Awareness Glalie V2")!!
          ),
          Pokemon.getByName("Staraptor")!!.species to listOf(
            Pokemon.getByName("Awareness Staraptor V1")!!,
            Pokemon.getByName("Awareness Staraptor V2")!!
          ),
          Pokemon.getByName("Kricketot")!!.species to listOf(
            Pokemon.getByName("Awareness Kricketot V1")!!,
            Pokemon.getByName("Awareness Kricketot V2")!!
          ),
          Pokemon.getByName("Gible")!!.species to listOf(
            Pokemon.getByName("Awareness Gible V1")!!,
            Pokemon.getByName("Awareness Gible V2")!!
          ),
          Pokemon.getByName("Krookodile")!!.species to listOf(
            Pokemon.getByName("Awareness Krookodile V1")!!,
            Pokemon.getByName("Awareness Krookodile V2")!!
          ),
          Pokemon.getByName("Zorua")!!.species to listOf(
            Pokemon.getByName("Awareness Zorua V1")!!,
            Pokemon.getByName("Awareness Zorua V2")!!
          ),
          Pokemon.getByName("Mienfoo")!!.species to listOf(
            Pokemon.getByName("Awareness Mienfoo V1")!!,
            Pokemon.getByName("Awareness Mienfoo V2")!!
          ),
          Pokemon.getByName("Thundurus")!!.species to listOf(
            Pokemon.getByName("Awareness Thundurus V1")!!,
            Pokemon.getByName("Awareness Thundurus V2")!!
          ),
          Pokemon.getByName("Swirlix")!!.species to listOf(
            Pokemon.getByName("Awareness Swirlix V1")!!,
            Pokemon.getByName("Awareness Swirlix V2")!!
          ),
          Pokemon.getByName("Clawitzer")!!.species to listOf(
            Pokemon.getByName("Awareness Clawitzer V1")!!,
            Pokemon.getByName("Awareness Clawitzer V2")!!
          ),
          Pokemon.getByName("Diancie")!!.species to listOf(
            Pokemon.getByName("Awareness Diancie V1")!!,
            Pokemon.getByName("Awareness Diancie V2")!!
          ),
          Pokemon.getByName("Rowlet")!!.species to listOf(
            Pokemon.getByName("Awareness Rowlet V1")!!,
            Pokemon.getByName("Awareness Rowlet V2")!!
          ),
          Pokemon.getByName("Toxapex")!!.species to listOf(
            Pokemon.getByName("Awareness Toxapex V1")!!,
            Pokemon.getByName("Awareness Toxapex V2")!!
          ),
          Pokemon.getByName("Dhelmise")!!.species to listOf(
            Pokemon.getByName("Awareness Dhelmise V1")!!,
            Pokemon.getByName("Awareness Dhelmise V2")!!
          ),
          Pokemon.getByName("Cramorant")!!.species to listOf(
            Pokemon.getByName("Awareness Cramorant V1")!!,
            Pokemon.getByName("Awareness Cramorant V2")!!
          ),
          Pokemon.getById(866)!!.species to listOf(
            Pokemon.getByName("Awareness Mr. Rime V1")!!,
            Pokemon.getByName("Awareness Mr. Rime V2")!!
          ),
        )
      ),
      EventPokemon( //mayflowers 2024 week 3 - space
        "Mayflowers 2024",
        parseDateTime("2024-05-20 07:00:00"),
        parseDateTime("2024-05-27 06:59:59"),
        mapOf(
          Pokemon.getByName("Wartortle")!!.species to listOf(
            Pokemon.getByName("Awareness Wartortle V1")!!,
            Pokemon.getByName("Awareness Wartortle V2")!!
          ),
          Pokemon.getByName("Pidgeotto")!!.species to listOf(
            Pokemon.getByName("Awareness Pidgeotto V1")!!,
            Pokemon.getByName("Awareness Pidgeotto V2")!!
          ),
          Pokemon.getByName("Grimer")!!.species to listOf(
            Pokemon.getByName("Awareness Grimer V1")!!,
            Pokemon.getByName("Awareness Grimer V2")!!
          ),
          Pokemon.getByName("Kingler")!!.species to listOf(
            Pokemon.getByName("Awareness Kingler V1")!!,
            Pokemon.getByName("Awareness Kingler V2")!!
          ),
          Pokemon.getByName("Elekid")!!.species to listOf(
            Pokemon.getByName("Awareness Elekid V1")!!,
            Pokemon.getByName("Awareness Elekid V2")!!
          ),
          Pokemon.getByName("Trapinch")!!.species to listOf(
            Pokemon.getByName("Awareness Trapinch V1")!!,
            Pokemon.getByName("Awareness Trapinch V2")!!
          ),
          Pokemon.getByName("Glalie")!!.species to listOf(
            Pokemon.getByName("Awareness Glalie V1")!!,
            Pokemon.getByName("Awareness Glalie V2")!!
          ),
          Pokemon.getByName("Staraptor")!!.species to listOf(
            Pokemon.getByName("Awareness Staraptor V1")!!,
            Pokemon.getByName("Awareness Staraptor V2")!!
          ),
          Pokemon.getByName("Kricketot")!!.species to listOf(
            Pokemon.getByName("Awareness Kricketot V1")!!,
            Pokemon.getByName("Awareness Kricketot V2")!!
          ),
          Pokemon.getByName("Gible")!!.species to listOf(
            Pokemon.getByName("Awareness Gible V1")!!,
            Pokemon.getByName("Awareness Gible V2")!!
          ),
          Pokemon.getByName("Krookodile")!!.species to listOf(
            Pokemon.getByName("Awareness Krookodile V1")!!,
            Pokemon.getByName("Awareness Krookodile V2")!!
          ),
          Pokemon.getByName("Zorua")!!.species to listOf(
            Pokemon.getByName("Awareness Zorua V1")!!,
            Pokemon.getByName("Awareness Zorua V2")!!
          ),
          Pokemon.getByName("Mienfoo")!!.species to listOf(
            Pokemon.getByName("Awareness Mienfoo V1")!!,
            Pokemon.getByName("Awareness Mienfoo V2")!!
          ),
          Pokemon.getByName("Thundurus")!!.species to listOf(
            Pokemon.getByName("Awareness Thundurus V1")!!,
            Pokemon.getByName("Awareness Thundurus V2")!!
          ),
          Pokemon.getByName("Swirlix")!!.species to listOf(
            Pokemon.getByName("Awareness Swirlix V1")!!,
            Pokemon.getByName("Awareness Swirlix V2")!!
          ),
          Pokemon.getByName("Clawitzer")!!.species to listOf(
            Pokemon.getByName("Awareness Clawitzer V1")!!,
            Pokemon.getByName("Awareness Clawitzer V2")!!
          ),
          Pokemon.getByName("Diancie")!!.species to listOf(
            Pokemon.getByName("Awareness Diancie V1")!!,
            Pokemon.getByName("Awareness Diancie V2")!!
          ),
          Pokemon.getByName("Rowlet")!!.species to listOf(
            Pokemon.getByName("Awareness Rowlet V1")!!,
            Pokemon.getByName("Awareness Rowlet V2")!!
          ),
          Pokemon.getByName("Toxapex")!!.species to listOf(
            Pokemon.getByName("Awareness Toxapex V1")!!,
            Pokemon.getByName("Awareness Toxapex V2")!!
          ),
          Pokemon.getByName("Dhelmise")!!.species to listOf(
            Pokemon.getByName("Awareness Dhelmise V1")!!,
            Pokemon.getByName("Awareness Dhelmise V2")!!
          ),
          Pokemon.getByName("Cramorant")!!.species to listOf(
            Pokemon.getByName("Awareness Cramorant V1")!!,
            Pokemon.getByName("Awareness Cramorant V2")!!
          ),
          Pokemon.getById(866)!!.species to listOf(
            Pokemon.getByName("Awareness Mr. Rime V1")!!,
            Pokemon.getByName("Awareness Mr. Rime V2")!!
          ),
        )
      ),
      EventPokemon( //mayflowers 2024 week 4 - pets
        "Mayflowers 2024",
        parseDateTime("2024-05-27 07:00:00"),
        parseDateTime("2024-06-03 06:59:59"),
        mapOf(
          Pokemon.getByName("Wartortle")!!.species to listOf(
            Pokemon.getByName("Awareness Wartortle V1")!!,
            Pokemon.getByName("Awareness Wartortle V2")!!
          ),
          Pokemon.getByName("Pidgeotto")!!.species to listOf(
            Pokemon.getByName("Awareness Pidgeotto V1")!!,
            Pokemon.getByName("Awareness Pidgeotto V2")!!
          ),
          Pokemon.getByName("Grimer")!!.species to listOf(
            Pokemon.getByName("Awareness Grimer V1")!!,
            Pokemon.getByName("Awareness Grimer V2")!!
          ),
          Pokemon.getByName("Kingler")!!.species to listOf(
            Pokemon.getByName("Awareness Kingler V1")!!,
            Pokemon.getByName("Awareness Kingler V2")!!
          ),
          Pokemon.getByName("Elekid")!!.species to listOf(
            Pokemon.getByName("Awareness Elekid V1")!!,
            Pokemon.getByName("Awareness Elekid V2")!!
          ),
          Pokemon.getByName("Trapinch")!!.species to listOf(
            Pokemon.getByName("Awareness Trapinch V1")!!,
            Pokemon.getByName("Awareness Trapinch V2")!!
          ),
          Pokemon.getByName("Glalie")!!.species to listOf(
            Pokemon.getByName("Awareness Glalie V1")!!,
            Pokemon.getByName("Awareness Glalie V2")!!
          ),
          Pokemon.getByName("Staraptor")!!.species to listOf(
            Pokemon.getByName("Awareness Staraptor V1")!!,
            Pokemon.getByName("Awareness Staraptor V2")!!
          ),
          Pokemon.getByName("Kricketot")!!.species to listOf(
            Pokemon.getByName("Awareness Kricketot V1")!!,
            Pokemon.getByName("Awareness Kricketot V2")!!
          ),
          Pokemon.getByName("Gible")!!.species to listOf(
            Pokemon.getByName("Awareness Gible V1")!!,
            Pokemon.getByName("Awareness Gible V2")!!
          ),
          Pokemon.getByName("Krookodile")!!.species to listOf(
            Pokemon.getByName("Awareness Krookodile V1")!!,
            Pokemon.getByName("Awareness Krookodile V2")!!
          ),
          Pokemon.getByName("Zorua")!!.species to listOf(
            Pokemon.getByName("Awareness Zorua V1")!!,
            Pokemon.getByName("Awareness Zorua V2")!!
          ),
          Pokemon.getByName("Mienfoo")!!.species to listOf(
            Pokemon.getByName("Awareness Mienfoo V1")!!,
            Pokemon.getByName("Awareness Mienfoo V2")!!
          ),
          Pokemon.getByName("Thundurus")!!.species to listOf(
            Pokemon.getByName("Awareness Thundurus V1")!!,
            Pokemon.getByName("Awareness Thundurus V2")!!
          ),
          Pokemon.getByName("Swirlix")!!.species to listOf(
            Pokemon.getByName("Awareness Swirlix V1")!!,
            Pokemon.getByName("Awareness Swirlix V2")!!
          ),
          Pokemon.getByName("Clawitzer")!!.species to listOf(
            Pokemon.getByName("Awareness Clawitzer V1")!!,
            Pokemon.getByName("Awareness Clawitzer V2")!!
          ),
          Pokemon.getByName("Diancie")!!.species to listOf(
            Pokemon.getByName("Awareness Diancie V1")!!,
            Pokemon.getByName("Awareness Diancie V2")!!
          ),
          Pokemon.getByName("Rowlet")!!.species to listOf(
            Pokemon.getByName("Awareness Rowlet V1")!!,
            Pokemon.getByName("Awareness Rowlet V2")!!
          ),
          Pokemon.getByName("Toxapex")!!.species to listOf(
            Pokemon.getByName("Awareness Toxapex V1")!!,
            Pokemon.getByName("Awareness Toxapex V2")!!
          ),
          Pokemon.getByName("Dhelmise")!!.species to listOf(
            Pokemon.getByName("Awareness Dhelmise V1")!!,
            Pokemon.getByName("Awareness Dhelmise V2")!!
          ),
          Pokemon.getByName("Cramorant")!!.species to listOf(
            Pokemon.getByName("Awareness Cramorant V1")!!,
            Pokemon.getByName("Awareness Cramorant V2")!!
          ),
          Pokemon.getById(866)!!.species to listOf(
            Pokemon.getByName("Awareness Mr. Rime V1")!!,
            Pokemon.getByName("Awareness Mr. Rime V2")!!
          ),
        )
      ),
      */
    )
  }
//  private val redeemableEventList by lazy {
//    listOf(
//      RedeemExclusivePokemon(
//        parseDateTime("2022-11-01 00:00:00"),
//        parseDateTime("2023-01-01 00:00:00"),
//        mapOf(
//          Pokemon.getByName("Bulbasaur")!!.species to listOf(
//            Pokemon.getByName("Winter Bulbasaur")!!,
//            Pokemon.getByName("Holiday Bulbasaur")!!
//          ),
//        )
//      )
//    )
//  }

  data class EventPokemon(
    val eventName: String,
    val startsAt: Long, //YYYY-MM-DD
    val endsAt: Long, //YYYY-MM-DD
    val catchableCustomPokemon: Map<Species, List<Pokemon>>,
  )

//  data class RedeemExclusivePokemon(
//    val startsAt: Long,
//    val endsAt: Long,
//    val redeemableCustomPokemon: Map<Species,List<Pokemon>>
//  )

  private fun parseDateTime(text: String): Long {
    return LocalDateTime.parse(text, dateTimePattern).atZone(ZoneOffset.UTC).toInstant().toEpochMilli()
  }

  fun handleCatching(spawnedSpecies: Species): Int? {
    val now = System.currentTimeMillis()
    for (eventPokemon in catchableEventList) {
      if (now in eventPokemon.startsAt..eventPokemon.endsAt) {
        eventPokemon.catchableCustomPokemon[spawnedSpecies]?.let {
          return it.randomOrNull()?.id
        }
      }
    }
    return null
  }

  fun isEventPokemon(pokemon: Pokemon): Boolean {
    return catchableEventList.any { e -> e.catchableCustomPokemon.any { (_, v) -> v.any { it.id == pokemon.id } } }
  }

  fun getCurrentCatchableEvents(): List<EventPokemon> {
    val now = System.currentTimeMillis()
    return catchableEventList.filter { now in it.startsAt..it.endsAt }
  }

  fun getCurrentEvent(): EventPokemon? {
    val now = System.currentTimeMillis()
    for (event in catchableEventList) {
      if (now in event.startsAt..event.endsAt) {
        return event
      }
    }
    return null
  }

//  fun getCurrentRedeemableEvents(): List<RedeemExclusivePokemon> {
//    val now = System.currentTimeMillis()
//    return redeemableEventList.filter {now in it.startsAt..it.endsAt}
//  }

  private val dateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
}
