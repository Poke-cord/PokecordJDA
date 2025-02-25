package xyz.pokecord.bot.modules.trade.commands

import dev.minn.jda.ktx.await
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object TradeCancelCommand: Command() {
  override val name = "cancel"

  @Executor
  suspend fun execute(
    context: ICommandContext
  ) {
    if (!context.hasStarted(true)) return

    val tradeState = context.getTradeState()
    if(tradeState == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.errors.notInTrade")
        ).build()
      ).queue()
      return
    }

    val initiator = if(tradeState.initiator.userId == context.author.id) tradeState.initiator else tradeState.receiver
    val partner = if(tradeState.initiator.userId == context.author.id) tradeState.receiver else tradeState.initiator

    val partnerUser = context.jda.retrieveUserById(partner.userId).await()
    val partnerData = context.bot.database.userRepository.getUser(partnerUser)

    context.bot.database.userRepository.incCredits(partnerData, partner.credits)
    context.bot.database.userRepository.incCredits(context.getUserData(), initiator.credits)

    context.bot.database.tradeRepository.endTrade(tradeState)

    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.trading.commands.cancel.embeds.cancelTrade.description",
          mapOf(
            "author" to context.author.asMention,
            "partner" to partnerUser.asMention
          )
        ),
        context.translate("modules.trading.commands.cancel.embeds.cancelTrade.title")
      ).build()
    ).queue()
  }
}