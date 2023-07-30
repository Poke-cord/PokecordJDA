package xyz.pokecord.bot.modules.transfer.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object TransferStartCommand : Command() {
  override val name: String = "start"

  @Executor
  suspend fun execute(
    context: ICommandContext,
  ) {
    if (!context.hasStarted(true)) return

    val transferState = context.getTransferState()
    if (transferState != null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.transfer.errors.alreadyInTransfer")
        ).build()
      ).queue()
      return
    }
    if (context.getTradeState() != null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("misc.errors.inState")
        ).build()
      ).queue()
      return
    }

    context.bot.database.transferRepository.createTransfer(context.author.id)

    context.reply(
      context.embedTemplates.normal(
        context.translate("modules.transfer.embeds.start.description"),
        context.translate("modules.transfer.embeds.start.title")
      ).build()
    ).queue()
  }
}