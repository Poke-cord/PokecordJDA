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
import xyz.pokecord.bot.core.managers.database.models.Listing
import xyz.pokecord.bot.utils.CountResult
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.extensions.awaitSuspending

class MarketRepository(
  database: Database,
  private val collection: CoroutineCollection<Listing>,
  private val cacheMap: RMapCacheAsync<String, String>
) : Repository(database) {
  override suspend fun createIndexes() {
    collection.createIndex(Indexes.ascending("id"))
    collection.createIndex(Indexes.ascending("ownerId"))
    collection.createIndex(Indexes.ascending("pokemon"))
    collection.createIndex(Indexes.ascending("price"))
    collection.createIndex(Indexes.ascending("sold"))
  }

  private suspend fun getListingCache(listingId: Int): Listing? {
    val json = cacheMap.getAsync(listingId.toString()).awaitSuspending() ?: return null
    return Json.decodeFromString(json)
  }

  private suspend fun setCacheListing(listing: Listing) {
    if (listing._isNew) {
      listing._isNew = false
      collection.insertOne(listing)
    }
    cacheMap.putAsync(listing.id.toString(), Json.encodeToString(listing.copy())).awaitSuspending()
  }

  suspend fun getListing(listingId: Int): Listing? {
    var listing: Listing? = getListingCache(listingId)
    if (listing == null) {
      listing = collection.findOne(Listing::id eq listingId)
      if (listing != null) {
        setCacheListing(listing)
      }
    }
    return listing
  }

  suspend fun createListing(listing: Listing, clientSession: ClientSession? = null) {
    if (clientSession == null) collection.insertOne(listing)
    else collection.insertOne(clientSession, listing)
    cacheMap.putAsync(listing.id.toString(), Json.encodeToString(listing.copy())).awaitSuspending()
  }

  suspend fun markSold(listing: Listing, buyerId: String, session: ClientSession) {
    listing.sold = true
    listing.soldTo = buyerId
    collection.updateOne(session, Listing::id eq listing.id, set(Listing::sold setTo true, Listing::soldTo setTo buyerId))
    setCacheListing(listing)
  }

  suspend fun markUnlisted(listing: Listing, session: ClientSession) {
    listing.unlisted = true
    collection.updateOne(session, Listing::id eq listing.id, set(Listing::unlisted setTo true))
    setCacheListing(listing)
  }

  suspend fun getListings(
    ownerId: String? = null,
    limit: Int? = 10,
    skip: Int? = 0,
    aggregation: MutableList<Bson> = mutableListOf()
  ): List<Listing> {
    if (ownerId != null) aggregation.add(match(Listing::ownerId eq ownerId))
    if (skip != null) aggregation.add(skip(skip))
    if (limit != null) aggregation.add(limit(limit))
    return collection.aggregate<Listing>(*aggregation.toTypedArray()).toList()
  }

  suspend fun getListingCount(
    ownerId: String? = null,
    aggregation: MutableList<Bson> = mutableListOf()
  ): Int {
    if (ownerId != null) aggregation.add(match(Auction::ownerId eq ownerId))
    val result = collection.aggregate<CountResult>(
      *aggregation.toTypedArray(),
      Aggregates.count("count")
    ).toList()
    if (result.isEmpty()) return 0
    return result.first().count
  }

  suspend fun getLatestListing(clientSession: ClientSession? = null): Listing? {
    return if (clientSession == null) {
      collection.find(EMPTY_BSON).sort(descending(Listing::id)).limit(1).first()
    } else {
      collection.find(clientSession, EMPTY_BSON).sort(descending(Listing::id)).limit(1).first()
    }
  }
}