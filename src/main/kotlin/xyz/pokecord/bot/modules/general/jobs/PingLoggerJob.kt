package xyz.pokecord.bot.modules.general.jobs

import org.slf4j.LoggerFactory
import xyz.pokecord.bot.core.structures.discord.Job

class PingLoggerJob : Job() {
  override val interval = 60000L
  override val name = "PingLogger"

  private val logger = LoggerFactory.getLogger(PingLoggerJob::class.java)

  override suspend fun execute() {
    logger.info("Gateway Ping is ${module.bot.jda.gatewayPing}ms!")
  }
}
