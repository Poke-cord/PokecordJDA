package xyz.pokecord

import dev.zihad.remotesharding.api.events.DisconnectEvent
import dev.zihad.remotesharding.api.events.MessageEvent
import dev.zihad.remotesharding.client.Client
import dev.zihad.remotesharding.messages.server.LoginOkMessage
import io.sentry.Sentry
import org.slf4j.LoggerFactory
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
        val shardCount = System.getenv("SHARD_COUNT")?.toIntOrNull() ?: 1
        val shardId = System.getenv("SHARD_ID")?.toIntOrNull() ?: 0
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
          bot.modules[it.name.lowercase()] = it
        }
        if (sharderHost != null && sharderPort != null) {
          val client = Client(sharderHost, sharderPort, token, shardId.toShort())
          client.on<DisconnectEvent> {
            bot.shutdown()
            Sentry.close()
          }
          client.on<MessageEvent.MessageReceivedEvent> {
            if (it.message is LoginOkMessage) {
              bot.cache.withIdentifyLock {
                bot.start(client.session?.shardCount, (it.message as LoginOkMessage).shardId.toInt())
                bot.jda.awaitReady()
                client.reportAsReady()
                Thread.sleep(5000)
              }
            }
          }
          client.start()
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
