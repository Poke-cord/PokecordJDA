package xyz.pokecord.bot.modules.transfer.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object TransferCancelCommand : Command() {
  override val name: String = "cancel"

  @Executor
  suspend fun execute(
    context: ICommandContext
  ) {
    if (!context.hasStarted(true)) return

    val transferState = context.getTransferState()
    if (transferState == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.transfer.errors.notInTransfer")
        ).build()
      ).queue()
      return
    }

    context.bot.database.transferRepository.endTransfer(transferState)

    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.transfer.embeds.cancelled.description",
        ),
        context.translate("modules.transfer.embeds.cancelled.title")
      ).build()
    ).queue()
  }
}