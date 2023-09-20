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
 class DaycareRepository(
  database: Database,
  private val collection: CoroutineCollection<Daycare>
) : Repository(database) {

   private val daycareCollection: CoroutineCollection<Daycare> =
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






