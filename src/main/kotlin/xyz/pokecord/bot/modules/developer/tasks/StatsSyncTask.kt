package xyz.pokecord.bot.modules.developer.tasks

import io.prometheus.client.Gauge
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import xyz.pokecord.bot.core.structures.PrometheusService
import xyz.pokecord.bot.core.structures.discord.ShardStatus
import xyz.pokecord.bot.core.structures.discord.base.Task
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.extensions.awaitSuspending

class StatsSyncTask : Task() {
  override val interval = 15_000L
  override val name = "RedisSync"

  private var lastCacheClearAt = 0L

  private val guildCount = Gauge
    .build("bot_misc_guild_count", "Guild Count")
    .register(PrometheusService.registry)

  private val pokemonCount = Gauge
    .build("bot_misc_pokemon_count", "Pokémon Count")
    .register(PrometheusService.registry)

  private val userCount = Gauge
    .build("bot_misc_user_count", "User Count")
    .register(PrometheusService.registry)

  private val botId by lazy {
    module.bot.shardManager.shards.first().selfUser.id
  }

  override suspend fun execute() {
    // Shard Status
    val now = System.currentTimeMillis()
    module.bot.shardManager.shards.forEach { jda ->
      val shardInfo = jda.shardInfo
      val shardStatus = ShardStatus(
        shardInfo.shardId,
        shardInfo.shardTotal,
        module.bot.hostname,
        jda.gatewayPing,
        jda.guildCache.size(),
        now
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

    val shardStatus = module.bot.cache.shardStatusMap.readAllValuesAsync().awaitSuspending().map { json ->
      Json.decodeFromString<ShardStatus>(json)
    }

    // Prometheus Stats
    guildCount.set(shardStatus.sumOf { it.guildCacheSize }.toDouble())
    pokemonCount.set(module.bot.database.pokemonRepository.getEstimatedPokemonCount().toDouble())
    userCount.set(module.bot.database.userRepository.getEstimatedUserCount().toDouble())

    // Top.gg Stats
    module.bot.topggClient?.let { topgg ->
      topgg.postServerCount(
        botId,
        shardStatus.map {
          it.guildCacheSize.toInt()
        }
      )
    }

    // Clear leaderboard cache
    if (lastCacheClearAt + 3_600_000L < now) {
      module.bot.database.userRepository.clearLeaderboardCache()
      lastCacheClearAt = now
    }
  }
}
