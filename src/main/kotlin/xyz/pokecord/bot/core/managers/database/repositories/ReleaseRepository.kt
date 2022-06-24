package xyz.pokecord.bot.core.managers.database.repositories

import com.mongodb.reactivestreams.client.ClientSession
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.managers.database.models.Release

class ReleaseRepository(
  database: Database,
  private val collection: CoroutineCollection<Release>,
) : Repository(database) {
  suspend fun createRelease(userId: String) {
    collection.insertOne(Release(userId))
  }

  suspend fun getRelease(userId: String): Release? {
    return collection.findOne(
      Release::ended eq false,
      Release::userId eq userId,
    )
  }

  suspend fun endRelease(release: Release) {
    collection.updateOne(
      Release::_id eq release._id, set(
        Release::ended setTo true
      )
    )
  }

  suspend fun addPokemon(release: Release, pokemonId: Id<OwnedPokemon>, session: ClientSession) {
    collection.updateOne(
      session,
      Release::_id eq release._id,
      push(
        Release::pokemon,
        pokemonId
      )
    )
  }

  suspend fun removePokemon(release: Release, pokemonId: Id<OwnedPokemon>, session: ClientSession) {
    collection.updateOne(
      session,
      Release::_id eq release._id,
      pull(
        Release::pokemon,
        pokemonId
      )
    )
  }
}
