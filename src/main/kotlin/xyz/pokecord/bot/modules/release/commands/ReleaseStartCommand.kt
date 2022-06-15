package xyz.pokecord.bot.modules.release.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object ReleaseStartCommand : Command() {
  override val name: String = "start"

  @Executor
  suspend fun execute(
    context: ICommandContext,
  ) {
    if (!context.hasStarted(true)) return

    val releaseState = context.getTradeState()
    if (releaseState != null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.pokemon.commands.release.errors.alreadyInRelease")
        ).build()
      ).queue()
      return
    }

    context.bot.database.tradeRepository.createTrade(context.author.id, context.author.id)

    val trade = context.getTradeState()
    if (trade != null) {
      context.bot.database.tradeRepository.setRelease(trade, context.author.id)
    }

    context.reply(
      context.embedTemplates.normal(
        context.translate("modules.pokemon.commands.release.embeds.center.releaseStarted.description",),
        context.translate("modules.pokemon.commands.release.embeds.center.releaseStarted.title")
      ).build()
    ).queue()
  }
}