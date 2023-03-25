package xyz.pokecord.bot.core.structures.pokemon

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object SpecialEvents {
  private val eventPokemonList by lazy {
    listOf(
      EventPokemon(
        parseDateTime("2024-06-15 00:00:00"),
        parseDateTime("2024-06-30 00:00:00"),
        mapOf(
          Pokemon.getByName("Charizard")!!.species to listOf(
            Pokemon.getByName("Pride Charizard V1")!!,
            Pokemon.getByName("Pride Charizard V2")!!
          )
        )
      )
    )
  }

  private data class EventPokemon(
    val startsAt: Long,
    val endsAt: Long,
    val customPokemon: Map<Species, List<Pokemon>>,
  )

  private fun parseDateTime(text: String): Long {
    return LocalDateTime.parse(text, dateTimePattern).atZone(ZoneOffset.UTC).toInstant().toEpochMilli()
  }

  fun handleCatching(spawnedSpecies: Species): Int? {
    val now = System.currentTimeMillis()
    for (eventPokemon in eventPokemonList) {
      if (now in eventPokemon.startsAt..eventPokemon.endsAt) {
        eventPokemon.customPokemon[spawnedSpecies]?.let {
          return it.randomOrNull()?.id
        }
      }
    }
    return null
  }

  fun isEventPokemon(pokemon: Pokemon): Boolean {
    return eventPokemonList.any { e -> e.customPokemon.any { (_, v) -> v.any { it.id == pokemon.id } } }
  }

  private val dateTimePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
}