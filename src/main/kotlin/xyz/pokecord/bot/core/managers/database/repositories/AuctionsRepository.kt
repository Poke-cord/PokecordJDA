package xyz.pokecord.bot.core.managers.database.repositories

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Indexes
import com.mongodb.reactivestreams.client.ClientSession
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.aggregate
import org.redisson.api.RMapCacheAsync
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.Auction
import xyz.pokecord.bot.core.managers.database.models.Bid
import xyz.pokecord.bot.utils.CountResult
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.extensions.awaitSuspending

class AuctionsRepository(
  database: Database,
  private val collection: CoroutineCollection<Auction>,
  private val cacheMap: RMapCacheAsync<String, String>
) : Repository(database) {
  override suspend fun createIndexes() {
    collection.createIndex(Indexes.ascending("id"))
    collection.createIndex(Indexes.ascending("ownerId"))
    collection.createIndex(Indexes.ascending("pokemon"))
    collection.createIndex(Indexes.ascending("endsAtTimestamp"))
    collection.createIndex(Indexes.ascending("ended"))
  }

  private suspend fun getCacheAuction(auctionId: Int): Auction? {
    val json = cacheMap.getAsync(auctionId.toString()).awaitSuspending() ?: return null
    return Json.decodeFromString(json)
  }

  private suspend fun setCacheAuction(auction: Auction) {
    if (auction._isNew) {
      auction._isNew = false
      collection.insertOne(auction)
    }
    cacheMap.putAsync(auction.id.toString(), Json.encodeToString(auction.copy())).awaitSuspending()
  }

  suspend fun createAuction(auction: Auction, clientSession: ClientSession? = null) {
    if (clientSession == null) collection.insertOne(auction)
    else collection.insertOne(clientSession, auction)
    cacheMap.putAsync(auction.id.toString(), Json.encodeToString(auction.copy())).awaitSuspending()
  }

  suspend fun getAuction(auctionId: Int): Auction? {
    var auction: Auction? = getCacheAuction(auctionId)
    if (auction == null) {
      auction = collection.findOne(Auction::id eq auctionId)
      if (auction != null) {
        setCacheAuction(auction)
      }
    }
    return auction
  }

  suspend fun endAuction(auction: Auction) {
    auction.ended = true
    collection.updateOne(Auction::id eq auction.id, set(Auction::ended setTo true))
    setCacheAuction(auction)
  }

  suspend fun insertBid(auction: Auction, bid: Bid) {
    auction.bids.add(bid)
    collection.updateOne(
      Auction::id eq auction.id,
      push(Auction::bids, bid)
    )
    setCacheAuction(auction)
  }

  suspend fun getAuctionList(
    ownerId: String? = null,
    limit: Int? = 15,
    skip: Int? = 0,
    aggregation: ArrayList<Bson> = arrayListOf()
  ): List<Auction> {
    if (skip != null) aggregation.add(skip(skip))
    if (limit != null) aggregation.add(limit(limit))
    if (ownerId != null) aggregation.add(Auction::ownerId eq ownerId)
    val result = collection.aggregate<Auction>(*aggregation.toTypedArray())
    return result.toList()
  }

  suspend fun getAuctionCount(
    ownerId: String? = null,
    aggregation: ArrayList<Bson> = arrayListOf()
  ): Int {
    if (ownerId != null) aggregation.add(Auction::ownerId eq ownerId)
    val result = collection.aggregate<CountResult>(
      *aggregation.toTypedArray(),
      Aggregates.count("count")
    ).toList()
    if (result.isEmpty()) return 0
    return result.first().count
  }

  suspend fun getLatestAuction(clientSession: ClientSession? = null): Auction? {
    return if (clientSession == null) {
      collection.find(EMPTY_BSON).sort(descending(Auction::id)).limit(1).first()
    } else {
      collection.find(clientSession, EMPTY_BSON).sort(descending(Auction::id)).limit(1).first()
    }
  }
}