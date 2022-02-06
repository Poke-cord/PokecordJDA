package xyz.pokecord.bot.core.managers

import kotlinx.coroutines.delay
import org.redisson.Redisson
import org.redisson.api.*
import org.redisson.config.Config
import org.slf4j.LoggerFactory
import xyz.pokecord.bot.utils.extensions.awaitSuspending
import xyz.pokecord.utils.RedissonNameMapper
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class Cache {
  private val logger = LoggerFactory.getLogger(Cache::class.java)

  private val redissonClient: RedissonClient

  private val commandRateLimitMap: RMapCacheAsync<String, Long>
  private val hasRunningCommandSet: RMapCacheAsync<String, Long>
  private val identifyLock: RBucketAsync<Boolean?>
  private val maintenanceStatus: RBucketAsync<Boolean?>
  private val currentGifts: RSetCacheAsync<String>

  val auctionMap: RMapCacheAsync<String, String>
  val battleRequestsMap: RMapCacheAsync<String, String>
  val guildMap: RMapCacheAsync<String, String>
  val guildSpawnChannelsMap: RSetMultimapCache<String, String>
  val listingMap: RMapCacheAsync<String, String>
  val leaderboardMap: RMapCacheAsync<String, String>
  val shardStatusMap: RMapCacheAsync<Int, String>
  val spawnChannelsMap: RMapCacheAsync<String, String>
  val userMap: RMapCacheAsync<String, String>
  val staffMemberIds: RSetCacheAsync<String>
  val staffMembersSet: RSetCacheAsync<String>

  init {
    val nameMapper = RedissonNameMapper(System.getenv("REDIS_NAME_MAPPER"))
    val clusterServersData = System.getenv("REDIS_CLUSTERS")
    val sentinelAddressesData = System.getenv("REDIS_SENTINEL_ADDRESSES")
    val sentinelMasterName = System.getenv("REDIS_SENTINEL_MASTER_NAME")
    val redisUrl = System.getenv("REDIS_URL")
    val redisPassword = System.getenv("REDIS_PASSWORD")

    val config = Config()
    if (!clusterServersData.isNullOrEmpty()) {
      config
        .useClusterServers()
        .addNodeAddress(*clusterServersData.split(",").toTypedArray())
        .setPassword(redisPassword)
        .nameMapper = nameMapper
    } else if (!sentinelAddressesData.isNullOrEmpty() && !sentinelMasterName.isNullOrEmpty()) {
      config.useSentinelServers()
        .setMasterName(sentinelMasterName)
        .addSentinelAddress(*sentinelAddressesData.split(",").toTypedArray())
        .setPassword(redisPassword)
        .nameMapper = nameMapper
    } else if (!redisUrl.isNullOrEmpty()) {
      config.useSingleServer()
        .setAddress(redisUrl)
        .setPassword(redisPassword)
        .nameMapper = nameMapper
    } else {
      throw Exception("Redis configuration not found in environment variables. Please configure Redis first.")
    }
    try {
      redissonClient = Redisson.create(config)
    } catch (e: Exception) {
      logger.error("Failed to create redisson client", e)
      exitProcess(1)
    }

    commandRateLimitMap = redissonClient.getMapCache("commandRateLimit")
    hasRunningCommandSet = redissonClient.getMapCache("hasRunningCommand")
    identifyLock = redissonClient.getBucket("identify")
    maintenanceStatus = redissonClient.getBucket("maintenanceStatus")
    currentGifts = redissonClient.getSetCache("currentGifts")

    auctionMap = redissonClient.getMapCache("auctions")
    battleRequestsMap = redissonClient.getMapCache("battleRequests")
    guildMap = redissonClient.getMapCache("guild")
    guildSpawnChannelsMap = redissonClient.getSetMultimapCache("guildSpawnChannels")
    listingMap = redissonClient.getMapCache("listings")
    leaderboardMap = redissonClient.getMapCache("leaderboard")
    shardStatusMap = redissonClient.getMapCache("shardStatus")
    spawnChannelsMap = redissonClient.getMapCache("spawnChannels")
    userMap = redissonClient.getMapCache("user")
    staffMemberIds = redissonClient.getSetCache("staffMemberIds")
    staffMembersSet = redissonClient.getSetCache("staffMembers")
  }

  suspend fun setRunningCommand(userId: String, isRunningCommand: Boolean): Long? {
    val future = if (isRunningCommand) {
      hasRunningCommandSet.putAsync(userId, System.currentTimeMillis())
    } else hasRunningCommandSet.removeAsync(userId)
    return future.awaitSuspending()
  }

  suspend fun isRunningCommand(userId: String): Boolean {
    val time = hasRunningCommandSet.getAsync(userId).awaitSuspending()
    return if (time != null && time + 60000 >= System.currentTimeMillis()) {
      true
    } else {
      hasRunningCommandSet.removeAsync(userId)
      false
    }
  }

  suspend fun getRateLimit(key: String): Long? {
    return commandRateLimitMap.getAsync(key).awaitSuspending()
  }

  suspend fun setRateLimit(key: String, value: Long, ttl: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS): Long? {
    return commandRateLimitMap.putAsync(key, value, ttl, timeUnit).awaitSuspending()
  }

  suspend fun removeRateLimit(key: String): Long? {
    return commandRateLimitMap.removeAsync(key).awaitSuspending()
  }

  suspend fun getMaintenanceStatus(): Boolean? {
    return maintenanceStatus.async.awaitSuspending()
  }

  suspend fun setMaintenanceStatus(maintenance: Boolean) {
    maintenanceStatus.setAsync(maintenance).awaitSuspending()
  }

  fun getAuctionIdLock(): RLock {
    return redissonClient.getFairLock("auction_id")
  }

  fun getAuctionLock(auctionId: Int): RLock {
    return redissonClient.getFairLock("auction-$auctionId")
  }

  fun getMarketIdLock(): RLock {
    return redissonClient.getFairLock("market_id")
  }

  fun getMarketLock(listingId: Int): RLock {
    return redissonClient.getFairLock("listing-$listingId")
  }

  suspend fun clearLocks() {
    currentGifts.deleteAsync().awaitSuspending()
    // Auction
    redissonClient.keys.deleteAsync("auction_id").awaitSuspending()
    redissonClient.keys.deleteByPatternAsync("auction-*").awaitSuspending()
    // Market
    redissonClient.keys.deleteAsync("market_id").awaitSuspending()
    redissonClient.keys.deleteByPatternAsync("listing-*").awaitSuspending()
    // Redisson stuff
    redissonClient.keys.deleteByPatternAsync("redisson*").awaitSuspending()
  }

  suspend fun withGiftLock(senderId: String, receiverId: String, block: suspend () -> Unit) {
    while (true) {
      val inProgress = currentGifts.containsAsync(senderId).awaitSuspending() || currentGifts.containsAsync(receiverId)
        .awaitSuspending()
      if (!inProgress) break
      else delay(100)
    }

    currentGifts.addAsync(senderId).awaitSuspending()
    currentGifts.addAsync(receiverId).awaitSuspending()
    block()
    currentGifts.removeAsync(senderId).awaitSuspending()
    currentGifts.removeAsync(receiverId).awaitSuspending()
  }

  fun getUserLock(userId: String): RLock {
    return redissonClient.getFairLock("user_lock-${userId}")
  }

  fun shutdown() {
    redissonClient.shutdown()
  }
}
