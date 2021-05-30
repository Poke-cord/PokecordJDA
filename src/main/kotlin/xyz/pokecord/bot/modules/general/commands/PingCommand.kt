package xyz.pokecord.bot.modules.general.commands

import dev.minn.jda.ktx.await
import kotlinx.coroutines.delay
import xyz.pokecord.bot.core.structures.discord.Command
import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext
import java.time.temporal.ChronoUnit

class PingCommand : Command() {
  override val name = "Ping"

  @Executor
  suspend fun execute(
    context: MessageReceivedContext
  ) {
    val embedTitle = context.translate("modules.general.commands.ping.embed.title")
    var embedDescription = context.translate(
      "modules.general.commands.ping.embed.description",
      mapOf(
        "apiPing" to "${context.jda.gatewayPing}ms",
        "ping" to "Calculating..."
      )
    )

    val message =
      context.reply(context.embedTemplates.normal(embedDescription, embedTitle).build()).await()
    embedDescription = context.translate(
      "modules.general.commands.ping.embed.description",
      mapOf(
        "apiPing" to "${context.jda.gatewayPing}ms",
        "ping" to "${context.message.timeCreated.until(message.timeCreated, ChronoUnit.MILLIS)}ms"
      )
    )
    message.editMessage(context.embedTemplates.normal(embedDescription, embedTitle).build())
      .reference(context.message)
      .mentionRepliedUser(false)
      .queue()
  }
}
