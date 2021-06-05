package xyz.pokecord.bot.modules.general.commands

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import xyz.pokecord.bot.core.structures.discord.MessageCommandContext
import xyz.pokecord.bot.core.structures.discord.SlashCommandContext
import xyz.pokecord.bot.core.structures.discord.base.BaseCommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import java.time.temporal.ChronoUnit

class PingCommand : Command() {
  override val name = "Ping"

  @Executor
  suspend fun execute(
    context: BaseCommandContext
  ) {
    val embedTitle = context.translate("modules.general.commands.ping.embed.title")
    var embedDescription = context.translate(
      "modules.general.commands.ping.embed.description",
      mapOf(
        "apiPing" to "${context.jda.gatewayPing}ms",
        "ping" to "Calculating..."
      )
    )

    val result =
      context.reply(context.embedTemplates.normal(embedDescription, embedTitle).build()).await()
    val message = when (context) {
      is MessageCommandContext -> result as Message
      is SlashCommandContext -> (result as InteractionHook).retrieveOriginal().await()
      else -> throw IllegalStateException("Unknown command context type ${context::class.java.name}")
    }
    embedDescription = context.translate(
      "modules.general.commands.ping.embed.description",
      mapOf(
        "apiPing" to "${context.jda.gatewayPing}ms",
        "ping" to "${context.timeCreated.until(message.timeCreated, ChronoUnit.MILLIS)}ms"
      )
    )
    message.editMessage(context.embedTemplates.normal(embedDescription, embedTitle).build())
      .queue()
  }
}
