package xyz.pokecord.bot.modules.trading.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object TradeAddCreditsCommand : Command() {
  override val name = "credits"
  override var aliases = arrayOf("c", "creds", "credits", "credit")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument amount: Int?
  ) {
    if (!context.hasStarted(true)) return

    println(amount)

    val tradeState = context.getTradeState()
    if (tradeState == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.commands.add.errors.notInTrade")
        ).build()
      ).queue()
      return
    }

    if (amount == null) {
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

    context.bot.database.userRepository.incCredits(userData, -amount)
    context.bot.database.tradeRepository.incCredits(tradeState, context.author.id, amount)

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