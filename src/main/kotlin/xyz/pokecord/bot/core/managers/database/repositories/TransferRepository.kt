package xyz.pokecord.bot.core.managers.database.repositories

import com.mongodb.reactivestreams.client.ClientSession
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.managers.database.models.Transfer

class TransferRepository(
  database: Database,
  private val collection: CoroutineCollection<Transfer>,
) : Repository(database) {
  override suspend fun createIndexes() {
    collection.createIndex(ascendingIndex(Transfer::userId))
  }

  suspend fun createTransfer(userId: String) {
    collection.insertOne(Transfer(userId))
  }

  suspend fun getTransfer(userId: String): Transfer? {
    return collection.findOne(
      Transfer::ended eq false,
      Transfer::userId eq userId,
    )
  }

  suspend fun endTransfer(Transfer: Transfer) {
    collection.updateOne(
      Transfer::_id eq Transfer._id, set(
        Transfer::ended setTo true
      )
    )
  }

  suspend fun addPokemon(Transfer: Transfer, pokemonId: Id<OwnedPokemon>, session: ClientSession) {
    collection.updateOne(
      session,
      Transfer::_id eq Transfer._id,
      push(
        Transfer::pokemon,
        pokemonId
      )
    )
  }

  suspend fun removePokemon(Transfer: Transfer, pokemonId: Id<OwnedPokemon>, session: ClientSession) {
    collection.updateOne(
      session,
      Transfer::_id eq Transfer._id,
      pull(
        Transfer::pokemon,
        pokemonId
      )
    )
  }
}
