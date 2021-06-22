package xyz.pokecord

import dev.minn.jda.ktx.await
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.ReadyEvent
import org.slf4j.LoggerFactory
import xyz.pokecord.bot.core.sharder.SharderClient
import xyz.pokecord.bot.core.sharder.packets.server.ShardInfoPacket
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.modules.developer.DeveloperModule
import xyz.pokecord.bot.modules.economy.EconomyModule
import xyz.pokecord.bot.modules.general.GeneralModule
import xyz.pokecord.bot.modules.pokemon.PokemonModule
import xyz.pokecord.bot.modules.profile.ProfileModule
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
        if (token.isNullOrBlank()) {
          logger.error("Token was not provided. Exiting...")
          exitProcess(0)
        }
        val shardCount = System.getenv("SHARD_COUNT")?.toIntOrNull()
        val shardId = System.getenv("SHARD_ID")?.toIntOrNull()
        val sharderHost: String? = System.getenv("SHARDER_HOST")
        val sharderPort = System.getenv("SHARDER_PORT")?.toIntOrNull()

        bot = Bot(token)
        val modules = listOf(
          PokemonModule(bot),
          GeneralModule(bot),
          ProfileModule(bot),
          EconomyModule(bot),
          DeveloperModule(bot)
        )
        modules.forEach {
          bot.modules[it.name.toLowerCase()] = it
        }
        if (sharderHost != null && sharderPort != null) {
          val client = SharderClient()
          client.logger.info("Connecting...")
          GlobalScope.launch(CoroutineName("SharderClient") + Dispatchers.IO) {
            try {
              client.connect()
              logger.info("Connected to the server at ${client.address}!")
              client.startReceiving()
              client.login()
            } catch (e: Throwable) {
              e.printStackTrace()
            }
          }
          GlobalScope.launch(CoroutineName("ShardInfoReceiver") + Dispatchers.Default) {
            try {
              while (true) {
                val packet = client.session.receivedPacketChannel.receive()
                if (packet is ShardInfoPacket) {
                  bot.start(packet.shardCount.toInt(), packet.shardIds.first().toInt())
                  bot.jda.await<ReadyEvent>()
                  client.reportAsReady()
                  break
                }
              }
            } catch (e: Throwable) {
              e.printStackTrace()
            }
          }
        } else {
          bot.start(shardCount, shardId)
        }
      } catch (e: Exception) {
        logger.error("Error occurred in App", e)
        Sentry.captureException(e)
      }
    }
  }
}
