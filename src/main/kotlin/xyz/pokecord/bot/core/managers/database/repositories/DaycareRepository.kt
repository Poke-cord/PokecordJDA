package xyz.pokecord.bot.core.managers.database.repositories

import org.litote.kmongo.eq
import xyz.pokecord.bot.core.structures.pokemon.Pokemon

class DaycareRepository {

  private val collection = KMongoCollection.getCollection<Pokemon>("daycare")

  suspend fun addPokemon(pokemon: Pokemon) {
    collection.insertOne(pokemon)
  }

  suspend fun removePokemon(name: String) {
    collection.deleteOne(Pokemon::name eq name)
  }

  suspend fun getPokemon(name: String): Pokemon? {
    return collection.findOne(Pokemon::name eq name)
  }

  suspend fun updatePokemon(pokemon: Pokemon) {
    collection.replaceOne(Pokemon::name eq pokemon.name, pokemon)
  }

  suspend fun getAllPokemon(): List<Pokemon> {
    return collection.find().toList()
  }

  suspend fun giveExpToAllPokemon(exp: Int) {
    collection.updateMany(
      Updates.inc("exp", exp)
    )
  }

}

// Usage

val repo = DaycareRepository()



// Give EXP


// Get pokemon


// Remove pokemon

