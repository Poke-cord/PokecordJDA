package xyz.pokecord.bot.modules.pokepedia.commands

import net.dv8tion.jda.api.utils.TimeFormat
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.SpecialEvents
import xyz.pokecord.bot.utils.EvolutionUtils
import java.time.LocalDateTime

object ForecastCommand : Command() {
  override val name = "Forecast"
  override var aliases = arrayOf("fc", "e", "events", "day", "night", "w", "weather", "event")

  @Executor
  suspend fun execute(
    context: ICommandContext
  ) {
    val time = if (EvolutionUtils.getCurrentTime(LocalDateTime.now()) == "day") "daytime" else "nighttime"
    val event = SpecialEvents.getCurrentEvent()

    if (event == null) {
      context.reply(
        context.embedTemplates.normal(
          context.translate("modules.pokemon.commands.forecast.description",
            mapOf(
              "timeOfDay" to time,
            )
          ),
          context.translate("modules.pokemon.commands.forecast.title")
        ).setFooter(context.translate("modules.pokemon.commands.forecast.footer"))
          .build()
      ).queue()
    } else {
      context.reply(
        context.embedTemplates.normal(
          context.translate("modules.pokemon.commands.forecast.eventdesc",
            mapOf(
              "eventName" to event.eventName,
              "eventEndTime" to TimeFormat.RELATIVE.format(event.endsAt),
              "timeOfDay" to time,
            )
          ),
          context.translate("modules.pokemon.commands.forecast.title")
        ).setFooter(context.translate("modules.pokemon.commands.forecast.footer"))
          .build()
      ).queue()
    }
  }
}
