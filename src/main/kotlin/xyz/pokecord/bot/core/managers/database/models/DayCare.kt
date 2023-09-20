package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import xyz.pokecord.bot.core.structures.pokemon.Nature
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.core.structures.pokemon.Species
import xyz.pokecord.bot.core.structures.pokemon.Stat
import xyz.pokecord.bot.utils.PokemonStats
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.Random


data class DayCare(var pokemon: Pokemon,
  var id: Int,
                   var index: Int,
                   var ownerId: String,
                   val shiny: Boolean,
                   val trainerId: String? = null,
                   var ivs: PokemonStats = OwnedPokemon.defaultIV(),
                   var evs: PokemonStats = OwnedPokemon.defaultEV(),
                   var xp: Int = 0,
                   var gender: Int = 2,
) {

  fun defaultIV(): PokemonStats {
    return PokemonStats(
      Random.nextInt(0, 32),
      Random.nextInt(0, 32),
      Random.nextInt(0, 32),
      Random.nextInt(0, 32),
      Random.nextInt(0, 32),
      Random.nextInt(0, 32)
    )
  }

  fun defaultEV(): PokemonStats {
    return PokemonStats(
      0,
      0,
      0,
      0,
      0,
      0
    )
  }

  private fun defaultLevel() = Random.nextInt(1, 6)

  private fun defaultNature() = Nature.random() ?: "Brave"


  companion object {
    fun getStatValue(
      id: Int,
      level: Int,
      stat: Stat,
      nature: Nature,
      iv: Int,
      ev: Int = (stat.getBaseEffortValue(id)!!),
      additionalEv: Int
    ): Int {
      val base = stat.getBaseValue(id)!!

      val statVal = if (stat.identifier == "hp") {
        if (id == 292) 1
        else floor(
          floor((floor((2 * base + iv + (ev + additionalEv) / 4).toDouble()) * level) / 100) +
              level +
              10
        ).roundToInt()
      } else {
        (floor(floor(floor((2 * base + iv + (ev + additionalEv) / 4).toDouble()) * level) / 100) + 5).roundToInt()
      }
      var multiplier = 1.0

      if (nature.increasedStatId == stat.id) multiplier += 0.1
      if (nature.decreasedStatId == stat.id) multiplier -= 0.1
      return floor(statVal * multiplier).roundToInt()
    }

    private fun defaultGender(speciesId: Int): Int {
      val genderRate = Species.getById(speciesId)?.genderRate
      return genderRate?.let {
        if (it == -1) -1 else if (Random.nextFloat() < (it * 12.5) / 100) 0 else 1
      } ?: 0
    }

  }
}