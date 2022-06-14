package xyz.pokecord.bot.modules.release.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object ReleaseCancelCommand : Command() {
  override val name: String = "cancel"

  @Executor
  suspend fun execute(
    context: ICommandContext
  ) {
    if (!context.hasStarted(true)) return

    val releaseState = context.getTradeState()
    if (releaseState == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.pokemon.commands.release.errors.notInRelease")
        ).build()
      ).queue()
      return
    }
    if(!releaseState.initiator.releaseTrade) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.pokemon.commands.release.errors.inTrade")
        ).build()
      ).queue()
      return
    }

    context.bot.database.tradeRepository.endTrade(releaseState)

    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.pokemon.commands.release.embeds.cancelled.description",
        ),
        context.translate("modules.pokemon.commands.release.embeds.cancelled.title")
      ).build()
    ).queue()
  }
}