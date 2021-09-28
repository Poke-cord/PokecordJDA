package xyz.pokecord.bot.modules.trading.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object TradeRemoveCreditsCommand: Command() {
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
          context.translate("modules.trading.commands.remove.errors.notInTrade")
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

    context.bot.database.userRepository.incCredits(context.getUserData(), amount)
    context.bot.database.tradeRepository.incCredits(tradeState, context.author.id, -amount)

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