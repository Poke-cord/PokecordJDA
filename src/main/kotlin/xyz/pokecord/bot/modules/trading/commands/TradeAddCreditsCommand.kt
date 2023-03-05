package xyz.pokecord.bot.modules.trading.commands

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
// import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.modules.staff.StaffCommand

object TradeAddCreditsCommand : StaffCommand() {
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

    if (amount == null || amount < 0) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.commands.add.errors.noNumberCredits")
        ).build()
      ).queue()
      return
    }

    val userData = context.getUserData()
    if (amount > userData.credits) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.commands.add.errors.notEnoughCredits")
        ).build()
      ).queue()
      return
    }

    val session = context.bot.database.startSession()
    session.use {
      session.startTransaction()
      context.bot.database.userRepository.incCredits(userData, -amount, session)
      context.bot.database.tradeRepository.incCredits(tradeState, context.author.id, amount, session)
      context.bot.database.tradeRepository.clearConfirmState(tradeState, session)
      session.commitTransactionAndAwait()
    }

    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.trading.commands.add.embeds.addCredits.description",
          "credits" to amount.toString()
        ),
        context.translate("modules.trading.commands.add.embeds.addCredits.title")
      ).build()
    ).queue()
  }
}