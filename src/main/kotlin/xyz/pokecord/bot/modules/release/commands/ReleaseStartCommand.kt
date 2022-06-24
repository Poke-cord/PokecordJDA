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

    val releaseState = context.getReleaseState()
    if (releaseState != null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.release.errors.alreadyInRelease")
        ).build()
      ).queue()
      return
    }
    if (context.getTradeState() != null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.release.errors.inTrade")
        ).build()
      ).queue()
      return
    }

    context.bot.database.releaseRepository.createRelease(context.author.id)

    context.reply(
      context.embedTemplates.normal(
        context.translate("modules.release.embeds.center.releaseStarted.description"),
        context.translate("modules.release.embeds.center.releaseStarted.title")
      ).build()
    ).queue()
  }
}