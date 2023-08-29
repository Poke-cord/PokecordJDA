package xyz.pokecord.bot.modules.nursery.commands

// PokemonBreeder.kt

import xyz.pokecord.models.Pokemon

class PokemonBreeder {

  companion object {

    fun breed(pokemon1: Pokemon, pokemon2: Pokemon): Pokemon {

      // Create new Pokemon
      val newPokemon = Pokemon()

      // Set name
      newPokemon.name = "${pokemon1.name}-${pokemon2.name}"

      // Set level
      newPokemon.level = (pokemon1.level + pokemon2.level) / 2

      // Set types
      newPokemon.types = listOf(pokemon1.types[0], pokemon2.types[0])

      // Set moves
      newPokemon.moves = (pokemon1.moves + pokemon2.moves).shuffled().take(4)

      // Return new Pokemon
      return newPokemon

    }

  }

}
