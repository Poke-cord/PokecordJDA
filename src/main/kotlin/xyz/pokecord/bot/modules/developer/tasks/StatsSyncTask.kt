package xyz.pokecord.bot.modules.developer.tasks

import io.prometheus.client.Gauge
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.dv8tion.jda.api.utils.MiscUtil
import xyz.pokecord.bot.core.structures.PrometheusService
import xyz.pokecord.bot.core.structures.discord.ShardStatus
import xyz.pokecord.bot.core.structures.discord.base.Task
import xyz.pokecord.bot.utils.Config
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.extensions.awaitSuspending

class StatsSyncTask : Task() {
  override val interval = 15_000L
  override val name = "RedisSync"

  private var lastCacheClearAt = 0L
  private var lastChannelUpdateAt = 0L

  private val guildCount = Gauge
    .build("bot_misc_guild_count", "Guild Count")
    .register(PrometheusService.registry)

  private val pokemonCount = Gauge
    .build("bot_misc_pokemon_count", "Pokémon Count")
    .register(PrometheusService.registry)

  private val creditsCount = Gauge
    .build("bot_misc_credits_count", "Credits Count")
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

    val guildsCount = shardStatus.sumOf { it.guildCacheSize }
    val usersCount = module.bot.database.userRepository.getEstimatedUserCount()

    // Prometheus Stats
    guildCount.set(guildsCount.toDouble())
    pokemonCount.set(module.bot.database.pokemonRepository.getEstimatedPokemonCount().toDouble())
    creditsCount.set(module.bot.database.userRepository.getTotalCreditsCount().toDouble())
    userCount.set(usersCount.toDouble())

    // Top.gg Stats
    module.bot.topggClient?.let { topgg ->
      topgg.postServerCount(
        botId,
        shardStatus.map {
          it.guildCacheSize.toInt()
        }
      )
    }

    // Clear users, guild and leaderboard cache every hour
    if (lastCacheClearAt + 3_600_000L < now) {
      module.bot.database.userRepository.clearLeaderboardCache()
      module.bot.database.userRepository.clearCache()
      module.bot.database.guildRepository.clearCache()
      lastCacheClearAt = now
    }

    // Update Stat Voice Channels every 5 minutes
    if (lastChannelUpdateAt + 300_000L < now) {
      val mainGuild = module.bot.shardManager.getGuildById(Config.mainServer)
      if (mainGuild != null && module.bot.shardManager.shards.any {
          it.shardInfo.shardId == MiscUtil.getShardForGuild(
            Config.mainServer,
            module.bot.shardManager.shardsTotal
          )
        }
      ) {
        val guildsChannel = mainGuild.getVoiceChannelById(Config.StatVoiceChannels.guilds)
        val usersChannel = module.bot.shardManager.getVoiceChannelById(Config.StatVoiceChannels.users)
        val monthlyVotesChannel = module.bot.shardManager.getVoiceChannelById(Config.StatVoiceChannels.monthlyVotes)

        usersChannel?.manager?.setName("\uD83D\uDC65 Players: $usersCount")?.queue()
        guildsChannel?.manager?.setName("\uD83D\uDCCA Servers: $guildsCount")?.queue()

        val votes = module.bot.topggClient?.getMonthlyVotes(botId)?.toString() ?: "Unavailable"
        monthlyVotesChannel?.manager?.setName("\uD83D\uDD25 Monthly Votes: $votes")?.queue()
      }
      lastChannelUpdateAt = now
    }
  }
}
