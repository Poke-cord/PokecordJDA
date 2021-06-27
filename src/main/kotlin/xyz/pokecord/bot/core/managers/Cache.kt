package xyz.pokecord.bot.core.managers

import org.redisson.Redisson
import org.redisson.api.RBucketAsync
import org.redisson.api.RMapCacheAsync
import org.redisson.api.RSetMultimapCache
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.slf4j.LoggerFactory
import xyz.pokecord.bot.utils.extensions.awaitSuspending
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class Cache {
  private val logger = LoggerFactory.getLogger(Cache::class.java)

  private val redissonClient: RedissonClient

  private val commandRateLimitMap: RMapCacheAsync<String, Long>
  private val hasRunningCommandSet: RMapCacheAsync<String, Long>
  private val identifyLock: RBucketAsync<Boolean?>
  private val maintenanceStatus: RBucketAsync<Boolean?>

  val guildMap: RMapCacheAsync<String, String>
  val guildSpawnChannelsMap: RSetMultimapCache<String, String>
  val lastCountedMessageMap: RMapCacheAsync<String, Long>
  val leaderboardMap: RMapCacheAsync<String, String>
  val shardStatusMap: RMapCacheAsync<Int, String>
  val spawnChannelsMap: RMapCacheAsync<String, String>
  val userMap: RMapCacheAsync<String, String>

  init {
    val config = Config()
    val clusterServersData = System.getenv("REDIS_CLUSTERS")
    val redisUrl = System.getenv("REDIS_URL")
    if (!clusterServersData.isNullOrEmpty()) {
      config
        .useClusterServers()
        .addNodeAddress(*clusterServersData.split(",").toTypedArray())
    } else if (!redisUrl.isNullOrEmpty()) {
      config.useSingleServer().address = redisUrl
      System.getenv("REDIS_PASSWORD")?.let { password ->
        config.useSingleServer().password = password
      }
    } else {
      throw Exception("Redis configuration not found in environment variables. Please configure Redis first.")
    }
    try {
      redissonClient = Redisson.create(config)
    } catch (e: Exception) {
      logger.error("Failed to create redisson client", e)
      exitProcess(0)
    }

    commandRateLimitMap = redissonClient.getMapCache("commandRateLimit")
    hasRunningCommandSet = redissonClient.getMapCache("hasRunningCommand")
    identifyLock = redissonClient.getBucket("identify")
    maintenanceStatus = redissonClient.getBucket("maintenanceStatus")

    guildMap = redissonClient.getMapCache("guild")
    guildSpawnChannelsMap = redissonClient.getSetMultimapCache("guildSpawnChannels")
    lastCountedMessageMap = redissonClient.getMapCache("lastCountedMessage")
    leaderboardMap = redissonClient.getMapCache("leaderboard")
    shardStatusMap = redissonClient.getMapCache("shardStatus")
    spawnChannelsMap = redissonClient.getMapCache("spawnChannels")
    userMap = redissonClient.getMapCache("user")
  }

  suspend fun setRunningCommand(userId: String, isRunningCommand: Boolean): Long? {
    val future = if (isRunningCommand) {
      hasRunningCommandSet.putAsync(userId, System.currentTimeMillis())
    } else hasRunningCommandSet.removeAsync(userId)
    return future.awaitSuspending()
  }

  suspend fun isRunningCommand(userId: String): Boolean {
    val time = hasRunningCommandSet.getAsync(userId).awaitSuspending()
    return if (time != null && time + 60000 < System.currentTimeMillis()) {
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

  fun withIdentifyLock(block: () -> Unit) {
    identifyLock.setAsync(true, 1, TimeUnit.SECONDS)
    block()
    identifyLock.deleteAsync()
  }

  fun shutdown() {
    redissonClient.shutdown()
  }
}
