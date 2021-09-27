package xyz.pokecord.bot.modules.trading.commands

import dev.minn.jda.ktx.await
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.Confirmation

object TradeStatusCommand: Command() {
  override val name = "status"

  @Executor
  suspend fun execute(
    context: ICommandContext
  ) {
    if (!context.hasStarted(true)) return

    val tradeState = context.getTradeState()
    if(tradeState == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.commands.status.errors.notInTrade")
        ).build()
      ).queue()
      return
    }

    val partner = if(tradeState.initiator.userId == context.author.id) tradeState.receiver else tradeState.initiator
    val initiator = if(tradeState.initiator.userId == context.author.id) tradeState.initiator else tradeState.receiver

    val partnerUser = context.jda.retrieveUserById(partner.userId).await()
    if(partnerUser == null) {
      val confirmation = Confirmation(context, initiator.userId)
      val confirmed = confirmation.result(
        context.embedTemplates.confirmation(
          context.translate("modules.trading.commands.status.errors.noPartnerFound.description"),
          context.translate("modules.trading.commands.status.errors.noPartnerFound.title")
        )
      )

      if(confirmed) {
        context.bot.database.tradeRepository.deleteTrade(context.author.id)
        context.reply(
          context.embedTemplates.normal(
            context.translate("modules.trading.commands.status.embeds.tradeEnded.description"),
            context.translate("modules.trading.commands.status.embeds.tradeEnded.title")
          ).build()
        ).queue()
      }

      return
    }

    context.reply(
      context.embedTemplates.normal(
        "testing"
      ).build()
    ).queue()
  }
}