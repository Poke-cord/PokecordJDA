package xyz.pokecord.bot.core.managers.database.repositories

import com.mongodb.client.model.Indexes
import org.litote.kmongo.coroutine.CoroutineCollection
import org.redisson.api.RMapCacheAsync
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.Auction
import xyz.pokecord.bot.core.managers.database.models.Bid

class AuctionsRepository(
  database: Database,
  private val collection: CoroutineCollection<Auction>,
  private val cacheMap: RMapCacheAsync<String, String>
): Repository(database) {
  override suspend fun createIndexes() {
    collection.createIndex(Indexes.ascending("ownerId"))
    collection.createIndex(Indexes.ascending("pokemon"))
    collection.createIndex(Indexes.ascending("endsAtTimestamp"))
    collection.createIndex(Indexes.ascending("ended"))
  }

  suspend fun getHighestBid(auction: Auction): Bid {
    return auction.bids.maxByOrNull { it.amount }!!
  }
}