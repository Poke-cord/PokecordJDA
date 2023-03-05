package xyz.pokecord.bot.modules.trading.commands

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
// import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.modules.staff.StaffCommand

object TradeRemoveCreditsCommand: StaffCommand() {
  override val name = "credits"
  override var aliases = arrayOf("c", "creds", "credits", "credit")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument amount: Int?
  ) {
    if (!context.hasStarted(true)) return

    val tradeState = context.getTradeState()
    if (tradeState == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.errors.notInTrade")
        ).build()
      ).queue()
      return
    }

    if (amount == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.commands.remove.errors.noNumberCredits")
        ).build()
      ).queue()
      return
    }

    val authorTradeState = if(tradeState.initiator.userId == context.author.id) tradeState.initiator else tradeState.receiver
    if(authorTradeState.credits < amount) {
      context.reply(
        context.embedTemplates.error(
          context.translate(
            "modules.trading.commands.remove.errors.notEnoughCredits",
            "credits" to amount.toString()
          )
        ).build()
      ).queue()
      return
    }

    val session = context.bot.database.startSession()
    session.use {
      session.startTransaction()
      context.bot.database.userRepository.incCredits(context.getUserData(), amount, session)
      context.bot.database.tradeRepository.incCredits(tradeState, context.author.id, -amount, session)
      context.bot.database.tradeRepository.clearConfirmState(tradeState, session)
      session.commitTransactionAndAwait()
    }

    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.trading.commands.remove.embeds.removeCredits.description",
          "credits" to amount.toString()
        ),
        context.translate("modules.trading.commands.remove.embeds.removeCredits.title")
      ).build()
    ).queue()
  }
}