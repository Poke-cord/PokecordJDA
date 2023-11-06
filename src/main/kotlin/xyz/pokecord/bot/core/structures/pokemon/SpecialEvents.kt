package xyz.pokecord.bot.core.structures.pokemon

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object SpecialEvents {
  private val catchableEventList by lazy {
    listOf(
      EventPokemon(
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
      )
    )
  }
  private val redeemableEventList by lazy {
    listOf(
      RedeemExclusivePokemon(
        parseDateTime("2022-11-01 00:00:00"),
        parseDateTime("2022-01-01 00:00:00"),
        mapOf(
          Pokemon.getByName("Bulbasaur")!!.species to listOf(
            Pokemon.getByName("Winter Bulbasaur")!!,
            Pokemon.getByName("Holiday Bulbasaur")!!
          ),
        )
      )
    )
  }

  data class EventPokemon(
    val startsAt: Long,
    val endsAt: Long,
    val catchableCustomPokemon: Map<Species, List<Pokemon>>,
  )

  data class RedeemExclusivePokemon(
    val startsAt: Long,
    val endsAt: Long,
    val redeemableCustomPokemon: Map<Species,List<Pokemon>>
  )

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
    val catchables = catchableEventList.any { e -> e.catchableCustomPokemon.any { (_, v) -> v.any { it.id == pokemon.id } } }
    val redeemables = redeemableEventList.any { e -> e.redeemableCustomPokemon.any { (_, v) -> v.any { it.id == pokemon.id } } }
    return catchables and redeemables  }

  fun getCurrentCatchableEvents(): List<EventPokemon> {
    val now = System.currentTimeMillis()
    return catchableEventList.filter { now in it.startsAt..it.endsAt }
  }

  fun getCurrentRedeemableEvents(): List<RedeemExclusivePokemon> {
    val now = System.currentTimeMillis()
    return redeemableEventList.filter {now in it.startsAt..it.endsAt}
  }

  private val dateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
}
