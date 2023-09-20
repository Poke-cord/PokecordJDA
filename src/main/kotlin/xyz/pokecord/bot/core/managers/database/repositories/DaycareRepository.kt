package xyz.pokecord.bot.core.managers.database.repositories

import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Updates
import org.litote.kmongo.KMongo
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.managers.database.models.VoteReward
import xyz.pokecord.bot.core.structures.pokemon.Pokemon



data class Daycare(
  val pokemon: Pokemon
)
abstract class DaycareRepository(
  database: Database,
  private val collection: CoroutineCollection<Daycare>
) : Repository(database) {
  private val releasedPokemonCollection: CoroutineCollection<Daycare> =
    database.database.getCollection("daycarePokemon")

  override suspend fun createIndexes() {
    collection.createIndex(Indexes.ascending("id"))
    collection.createIndex(Indexes.ascending("ownerId"))
    collection.createIndex(Indexes.ascending("pokemon"))
    collection.createIndex(Indexes.ascending("daycareTime"))
    collection.createIndex(Indexes.ascending("xp"))
  }

  suspend fun addPokemon(pokemon: Pokemon) {
    collection.insertOne(Daycare(pokemon))
  }

  suspend fun getPokemon(name: String): Pokemon? {
    val daycare = collection.findOne(Daycare::pokemon.name eq name)
    return null
  }

  suspend fun removePokemon(name: String) {
    collection.deleteOne(Pokemon::name eq name)
  }

  suspend fun getAllPokemon(): List<Daycare> {
    return collection.find().toList()
  }

  suspend fun giveExpToAllPokemon(exp: Int) {
    collection.updateMany(
      Updates.inc("exp", exp)
    )
  }

}

private fun <T : Any> CoroutineCollection<T>.findOne(unit: Unit) {

}

private infix fun String.eq(name: String) {

}


