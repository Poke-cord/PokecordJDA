package xyz.pokecord.bot.modules.pokepedia.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.EvolutionUtils
import java.time.LocalDateTime

object ForecastCommand : Command() {
  override val name = "Forecast"
  override var aliases = arrayOf("fc", "day", "night", "w", "weather")

  @Executor
  suspend fun execute(
    context: ICommandContext
  ) {
    val time = if (EvolutionUtils.getCurrentTime(LocalDateTime.now()) == "day") "daytime" else "nighttime"

    context.reply(
      context.embedTemplates.normal(
        context.translate("modules.pokemon.commands.forecast.description",
          mapOf("timeOfDay" to time)
        ),
        context.translate("modules.pokemon.commands.forecast.title")
      ).setFooter(context.translate("modules.pokemon.commands.forecast.footer"))
        .build()
    ).queue()
  }
}
