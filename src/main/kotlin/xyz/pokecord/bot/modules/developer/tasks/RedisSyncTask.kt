package xyz.pokecord.bot.modules.developer.tasks

import io.prometheus.client.Gauge
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import xyz.pokecord.bot.core.structures.PrometheusService
import xyz.pokecord.bot.core.structures.discord.ShardStatus
import xyz.pokecord.bot.core.structures.discord.base.Task
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.extensions.awaitSuspending

class RedisSyncTask : Task() {
  override val interval = 15_000L
  override val name = "RedisSync"

  private val guildCount = Gauge
    .build("bot_misc_guild_count", "Guild Count")
    .labelNames("hostname")
    .register(PrometheusService.registry)

  private val pokemonCount = Gauge
    .build("bot_misc_pokemon_count", "Pokémon Count")
    .labelNames("hostname")
    .register(PrometheusService.registry)

  private val userCount = Gauge
    .build("bot_misc_user_count", "User Count")
    .labelNames("hostname")
    .register(PrometheusService.registry)

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

    // Prometheus Stats
    guildCount.labels(module.bot.hostname).set(
      module.bot.cache.shardStatusMap.readAllValuesAsync().awaitSuspending()
        .map { json ->
          Json.decodeFromString<ShardStatus>(json)
        }.sumOf { it.guildCacheSize }.toDouble()
    )
    pokemonCount.labels(module.bot.hostname)
      .set(module.bot.database.pokemonRepository.getEstimatedPokemonCount().toDouble())
    userCount.labels(module.bot.hostname).set(module.bot.database.userRepository.getEstimatedUserCount().toDouble())
  }
}
