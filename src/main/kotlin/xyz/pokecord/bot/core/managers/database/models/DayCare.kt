package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import xyz.pokecord.bot.core.structures.pokemon.Nature
import xyz.pokecord.bot.utils.PokemonStats
import kotlin.random.Random

@Serializable
data class DayCare(var id: Int,
                   var index: Int,
                   var ownerId: String,
                   val shiny: Boolean,
                   val trainerId: String? = null,
                   var level: Int = OwnedPokemon.defaultLevel(),
                   var nature: String = OwnedPokemon.defaultNature(),
                   var ivs: PokemonStats = OwnedPokemon.defaultIV(),
                   var evs: PokemonStats = OwnedPokemon.defaultEV(),
                   var xp: Int = 0,
                   var gender: Int = 2,
                   val heldItemId: Int = 0,
                   var moves: MutableList<Int> = mutableListOf(),
                   var favorite: Boolean = false,
                   val rewardClaimed: Boolean = false,
                   val timestamp: Long = System.currentTimeMillis(),
                   val sticky: Boolean = false,
                   var nickname: String? = null,
                   val formId: Int? = null,
                   @Contextual val _id: Id<OwnedPokemon> = newId())

{

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

}