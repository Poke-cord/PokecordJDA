package xyz.pokecord.bot.modules.developer.tasks

import kotlinx.serialization.encodeToString
import xyz.pokecord.bot.core.structures.discord.ShardStatus
import xyz.pokecord.bot.core.structures.discord.base.Task
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.extensions.awaitSuspending

class RedisSyncTask : Task() {
  override val interval = 15_000L
  override val name = "RedisSync"

  override suspend fun execute() {
    // Shard Status
    module.bot.shardManager.shards.forEach { jda ->
      val shardInfo = jda.shardInfo
      val shardStatus = ShardStatus(
        shardInfo.shardId,
        shardInfo.shardTotal,
        module.bot.hostname,
        jda.gatewayPing,
        jda.guildCache.size(),
        System.currentTimeMillis()
      )
      module.bot.cache.shardStatusMap.putAsync(
        shardInfo.shardId,
        Json.encodeToString(shardStatus)
      ).awaitSuspending()
    }

    // Maintenance Status
    val maintenanceStatus = module.bot.cache.getMaintenanceStatus()
    maintenanceStatus?.let {
      if (it != module.bot.maintenance) {
        module.bot.toggleMaintenance()
      }
    }
  }
}
