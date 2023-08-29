package xyz.pokecord

import dev.minn.jda.ktx.injectKTX
import dev.zihad.remotesharding.client.Client
import io.sentry.Sentry
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.slf4j.LoggerFactory
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.modules.auctions.AuctionsModule
import xyz.pokecord.bot.modules.battle.BattleModule
import xyz.pokecord.bot.modules.developer.DeveloperModule
import xyz.pokecord.bot.modules.economy.EconomyModule
import xyz.pokecord.bot.modules.general.GeneralModule
import xyz.pokecord.bot.modules.market.MarketModule
import xyz.pokecord.bot.modules.nursery.NurseryModule
import xyz.pokecord.bot.modules.pokemon.PokemonModule
import xyz.pokecord.bot.modules.profile.ProfileModule
import xyz.pokecord.bot.modules.release.ReleaseModule
import xyz.pokecord.bot.modules.staff.StaffModule
import xyz.pokecord.bot.modules.trading.TradingModule
import xyz.pokecord.migration.Migration
import kotlin.system.exitProcess

object App {
  private val logger = LoggerFactory.getLogger(App::class.java)

  lateinit var bot: Bot

  @JvmStatic
  fun main(args: Array<String>) {
    val migrationMode = System.getenv("MIGRATION_MODE")
    if (migrationMode != null) {
      Migration.main(args)
    } else {
      try {
        val token = System.getenv("BOT_TOKEN")
        val topggToken = System.getenv("TOPGG_TOKEN")
        val encryptionKey = System.getenv("ENCRYPTION_KEY")
        if (token.isNullOrBlank()) {
          logger.error("Token was not provided. Exiting...")
          exitProcess(1)
        }
        if (topggToken.isNullOrBlank()) {
          logger.warn("Top.gg token was not provided. Stat posting will be disabled.")
        }
        if (encryptionKey.isNullOrBlank()) {
          logger.error("Encryption key was not provided. Exiting...")
          exitProcess(1)
        }
        val shardCount = System.getenv("SHARD_COUNT")?.toIntOrNull() ?: 1
        val shardId = System.getenv("SHARD_ID")?.toIntOrNull() ?: 0
        val sharderHost: String? = System.getenv("SHARDER_HOST")
        val sharderPort = System.getenv("SHARDER_PORT")?.toIntOrNull()
        val shardCapacity = System.getenv("SHARD_CAPACITY")?.toIntOrNull() ?: 1

        bot = Bot(token, topggToken)
        val modules = listOf(
          PokemonModule(bot),
          GeneralModule(bot),
          ProfileModule(bot),
          EconomyModule(bot),
          BattleModule(bot),
          AuctionsModule(bot),
          MarketModule(bot),
          TradingModule(bot),
          StaffModule(bot),
          DeveloperModule(bot),
          ReleaseModule(bot),
          NurseryModule(bot)
        )
        modules.forEach {
          bot.modules[it.name.lowercase()] = it
        }
        val shardManagerBuilder = DefaultShardManagerBuilder
          .createLight(token)
          .enableCache(CacheFlag.MEMBER_OVERRIDES)
          .injectKTX()
          .setStatus(OnlineStatus.INVISIBLE)
          .setActivity(Activity.playing("Initializing..."))
        if (sharderHost != null && sharderPort != null) {
          val client = Client(shardManagerBuilder, sharderHost, sharderPort, token, encryptionKey, shardCapacity)
          bot.start(shardManagerBuilder)
          client.onShardManagerBuild {
            bot.shardManager = it
          }
          client.start()
        } else {
          shardManagerBuilder.setShardsTotal(shardCount).setShards(shardId)
          bot.start(shardManagerBuilder, true)
        }
      } catch (e: Exception) {
        logger.error("Error occurred in App", e)
        Sentry.captureException(e)
      }
    }
  }
}
