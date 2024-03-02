package xyz.pokecord.bot.modules.general.commands

import dev.minn.jda.ktx.await
import xyz.pokecord.bot.core.structures.discord.base.BaseCommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

class PingCommand : Command() {
  override val name = "Ping"

  @Executor
  suspend fun execute(
    context: BaseCommandContext
  ) {
    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.general.commands.ping.embed.description",
          mapOf(
            "apiPing" to "${context.jda.gatewayPing}ms",
            "ping" to "${context.jda.restPing.await()}ms"
          )
        ),
        context.translate("modules.general.commands.ping.embed.title")
      ).setFooter(context.translate("modules.general.commands.ping.embed.footer")
      ).build()
    ).queue()
  }
}
