package xyz.pokecord.bot.modules.developer.tasks

import kotlinx.serialization.encodeToString
import xyz.pokecord.bot.core.structures.discord.ShardStatus
import xyz.pokecord.bot.core.structures.discord.Task
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.extensions.awaitSuspending

class RedisSyncTask : Task() {
  override val interval = 15_000L
  override val name = "RedisSync"

  private val hostname = System.getenv("HOSTNAME") ?: System.getenv("COMPUTERNAME") ?: "Unknown"

  override suspend fun execute() {
    // Shard Status
    val shardInfo = module.bot.jda.shardInfo
    val shardStatus = ShardStatus(
      shardInfo.shardId,
      shardInfo.shardTotal,
      hostname,
      module.bot.jda.gatewayPing,
      module.bot.jda.guildCache.size(),
      System.currentTimeMillis()
    )
    module.bot.cache.shardStatusMap.putAsync(
      shardInfo.shardId,
      Json.encodeToString(shardStatus)
    ).awaitSuspending()

    // Maintenance Status
    val maintenanceStatus = module.bot.cache.getMaintenanceStatus()
    if (maintenanceStatus != module.bot.maintenance) {
      module.bot.toggleMaintenance()
    }
  }
}
