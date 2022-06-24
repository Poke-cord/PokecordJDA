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

    val releaseState = context.getReleaseState()
    if (releaseState == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.release.errors.notInRelease")
        ).build()
      ).queue()
      return
    }

    context.bot.database.releaseRepository.endRelease(releaseState)

    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.release.embeds.cancelled.description",
        ),
        context.translate("modules.release.embeds.cancelled.title")
      ).build()
    ).queue()
  }
}