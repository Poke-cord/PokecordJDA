package xyz.pokecord.bot.core.managers.database.repositories

import com.mongodb.client.model.Indexes
import com.mongodb.reactivestreams.client.ClientSession
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import org.litote.kmongo.replaceOne
import org.litote.kmongo.set
import org.litote.kmongo.setTo
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.SpawnChannel
import xyz.pokecord.bot.utils.Json

class SpawnChannelRepository(
  database: Database,
  private val collection: CoroutineCollection<SpawnChannel>,
//  private val cacheMap: RMapCacheAsync<String, String>,
//  private val guildSpawnChannelCacheMap: RSetMultimapCache<String, String>
) : Repository(database) {
  private val cacheMap = mutableMapOf<String, String>()
  private val guildSpawnChannelCacheMap = mutableMapOf<String, MutableSet<String>>()

  override suspend fun createIndexes() {
    collection.createIndex(Indexes.ascending("guildId"))
  }

  private suspend fun getCacheSpawnChannel(id: String): SpawnChannel? {
//    val json = cacheMap.getAsync(id).awaitSuspending()
//      ?: return null
    val json = cacheMap[id] ?: return null
    return Json.decodeFromString(json)
  }

  private suspend fun setCacheSpawnChannel(spawnChannel: SpawnChannel) {
    cacheMap[spawnChannel.id] = Json.encodeToString(spawnChannel)
    guildSpawnChannelCacheMap.getOrDefault(spawnChannel.guildId, mutableSetOf()).add(spawnChannel.id)
//    cacheMap.putAsync(spawnChannel.id, Json.encodeToString(spawnChannel)).awaitSuspending()
//    guildSpawnChannelCacheMap.putAsync(spawnChannel.guildId, spawnChannel.id).awaitSuspending()
  }

  private suspend fun removeCacheSpawnChannel(spawnChannel: SpawnChannel) {
    cacheMap.remove(spawnChannel.id)
    guildSpawnChannelCacheMap[spawnChannel.guildId]?.remove(spawnChannel.id)
//    cacheMap.removeAsync(spawnChannel.id).awaitSuspending()
//    guildSpawnChannelCacheMap.removeAsync(spawnChannel.guildId, spawnChannel.id).awaitSuspending()
  }

  private suspend fun getCacheSpawnChannels(guildId: String): List<SpawnChannel>? {
    val spawnChannelIds = guildSpawnChannelCacheMap.getOrDefault(guildId, mutableSetOf())
    val map = spawnChannelIds.mapNotNull { cacheMap[it] }
    return map.map {
      Json.decodeFromString(it)
    }
//  val spawnChannelIds = guildSpawnChannelCacheMap.getAllAsync(guildId).awaitSuspending().toSet()
//    val map = cacheMap.getAllAsync(spawnChannelIds).awaitSuspending() ?: return null
//    return map.values.map {
//      Json.decodeFromString(it)
//    }
  }

  private suspend fun setCacheSpawnChannels(guildId: String, spawnChannels: List<SpawnChannel>) {
    val spawnChannelIds = spawnChannels.map { it.id }.toSet()
    if (spawnChannelIds.isNotEmpty()) guildSpawnChannelCacheMap.getOrDefault(guildId, mutableSetOf())
      .addAll(spawnChannelIds)
    cacheMap.putAll(
      spawnChannels.associate { it.id to Json.encodeToString(it) }
    )
//    val spawnChannelIds = spawnChannels.map { it.id }.toSet()
//    if (spawnChannelIds.isNotEmpty()) guildSpawnChannelCacheMap.putAllAsync(guildId, spawnChannelIds).awaitSuspending()
//    cacheMap.putAllAsync(
//      spawnChannels.associate { it.id to Json.encodeToString(it) }
//    ).awaitSuspending()
  }

  suspend fun getSpawnChannel(id: String): SpawnChannel? {
    var spawnChannel = getCacheSpawnChannel(id)
    if (spawnChannel == null) {
      spawnChannel = collection.findOne(SpawnChannel::id eq id)
      if (spawnChannel != null) setCacheSpawnChannel(spawnChannel)
    }
    return spawnChannel
  }

  suspend fun getSpawnChannels(guildId: String): List<SpawnChannel> {
    var spawnChannels = getCacheSpawnChannels(guildId)
    if (spawnChannels == null || spawnChannels.isEmpty()) {
      spawnChannels = collection.find(SpawnChannel::guildId eq guildId).toList()
      setCacheSpawnChannels(guildId, spawnChannels)
    }
    return spawnChannels
  }

  suspend fun removeSpawnChannel(spawnChannel: SpawnChannel) {
    removeCacheSpawnChannel(spawnChannel)
    collection.deleteOne(SpawnChannel::id eq spawnChannel.id)
  }

  suspend fun setSpawnChannel(spawnChannel: SpawnChannel) {
    setCacheSpawnChannel(spawnChannel)
    collection.save(spawnChannel)
  }

  suspend fun setSpawnChannels(guildId: String, spawnChannels: List<SpawnChannel>) {
    setCacheSpawnChannels(guildId, spawnChannels)
    if (spawnChannels.isNotEmpty()) {
      collection.bulkWrite(
        *spawnChannels.map {
          replaceOne(SpawnChannel::id eq it.id, it)
        }.toTypedArray()
      )
    } else {
      collection.deleteMany(
        SpawnChannel::guildId eq guildId
      )
    }
  }

  suspend fun updateMessageCount(spawnChannel: SpawnChannel): Boolean {
    val updated =
      collection.updateOneById(spawnChannel._id, set(SpawnChannel::sentMessages setTo spawnChannel.sentMessages))
        .wasAcknowledged()
    if (updated) setCacheSpawnChannel(spawnChannel)
    return updated
  }

  suspend fun updateDetails(spawnChannel: SpawnChannel): Boolean {
    val updated =
      collection.updateOneById(
        spawnChannel._id,
        set(
          SpawnChannel::sentMessages setTo spawnChannel.sentMessages,
          SpawnChannel::requiredMessages setTo spawnChannel.requiredMessages,
          SpawnChannel::spawned setTo spawnChannel.spawned
        )
      )
        .wasAcknowledged()
    if (updated) setCacheSpawnChannel(spawnChannel)
    return updated
  }

  suspend fun despawn(spawnChannel: SpawnChannel, clientSession: ClientSession? = null): Boolean {
    spawnChannel.spawned = 0
    val updated =
      (if (clientSession != null) collection.updateOneById(spawnChannel._id, set(SpawnChannel::spawned setTo 0))
      else collection.updateOneById(spawnChannel._id, set(SpawnChannel::spawned setTo 0))).wasAcknowledged()
    if (updated) setCacheSpawnChannel(spawnChannel)
    return updated
  }
}
