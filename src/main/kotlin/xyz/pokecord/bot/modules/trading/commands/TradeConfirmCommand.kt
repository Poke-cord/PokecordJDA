package xyz.pokecord.bot.modules.trading.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object TradeConfirmCommand: Command() {
  override val name = "confirm"

  @Executor
  suspend fun execute(context: ICommandContext) {
    if (!context.hasStarted(true)) return

    val tradeState = context.getTradeState()
    if (tradeState == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.commands.add.errors.notInTrade")
        ).build()
      ).queue()
      return
    }

    if(
      tradeState.initiator.credits == 0 &&
      tradeState.initiator.pokemon.isEmpty() &&
      tradeState.receiver.credits == 0 &&
      tradeState.receiver.pokemon.isEmpty()
    ) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.commands.confirm.errors.emptyTrade")
        ).build()
      ).queue()
      return
    }


    val partnerTradeState = if(tradeState.initiator.userId == context.author.id) tradeState.receiver else tradeState.initiator
    if(partnerTradeState.confirmed) {
      // The trade is finished
      // Transfer all the stuff

      context.reply(
        context.embedTemplates.normal(
          "testing"
        ).build()
      ).queue()
    } else {
      context.bot.database.tradeRepository.confirm(tradeState, context.author.id)

      context.reply(
        context.embedTemplates.normal(
          context.translate("modules.trading.commands.confirm.embeds.partConfirmed.description"),
          context.translate("modules.trading.commands.confirm.embeds.partConfirmed.title")
        ).build()
      ).queue()
    }
  }
}