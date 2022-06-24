package xyz.pokecord.bot.modules.trading.commands

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.entities.User
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.utils.Confirmation
import java.util.concurrent.TimeUnit

object TradeCommand : ParentCommand() {
  override val childCommands =
    mutableListOf(TradeAddCommand, TradeRemoveCommand, TradeCancelCommand, TradeStatusCommand, TradeConfirmCommand)
  override val name = "Trade"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(optional = false, name = "partner") partner: User?
  ) {
    if (partner == null) {
      super.execute(context)
      return
    }
    if (!context.hasStarted(true)) return

    val tradeState = context.getTradeState()
    if (tradeState != null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.commands.trade.errors.alreadyInTrade")
        ).build()
      ).queue()
      return
    }

    if (context.getReleaseState() != null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.errors.inRelease")
        ).build()
      ).queue()
      return
    }

    if (partner.id == context.author.id) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.commands.trade.errors.tradingYourself")
        ).build()
      ).queue()
      return
    }

    val partnerData = context.bot.database.userRepository.getUser(partner)
    if (partnerData.selected == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate(
            "modules.trading.commands.trade.errors.partnerHasntStarted",
            "partner" to partner.asMention
          )
        ).build()
      ).queue()
    }

    if (!context.hasMention(partner.id)) {
      val mentionMsg = context.channel.sendMessage(partner.asMention).await()
      mentionMsg.delete().queueAfter(3000, TimeUnit.MILLISECONDS)
    }

    val confirmation = Confirmation(context, partner.id)
    val confirmed = confirmation.result(
      context.embedTemplates.confirmation(
        context.translate(
          "modules.trading.commands.trade.confirmation.description",
          mapOf(
            "trader" to context.author.asMention,
            "traded" to partner.asMention
          )
        ),
        context.translate(
          "modules.trading.commands.trade.confirmation.title"
        )
      )
    )

    if (confirmed) {
      context.bot.database.tradeRepository.createTrade(context.author.id, partner.id)
      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.trading.commands.trade.tradeStarted.description",
            mapOf(
              "partner" to partner.asMention,
              "author" to context.author.asMention
            )
          ),
          context.translate("modules.trading.commands.trade.tradeStarted.title")
        ).build()
      ).queue()
    } else {
      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.trading.commands.trade.tradeCancelled.description",
            "partner" to partner.asMention
          ),
          context.translate("modules.trading.commands.trade.tradeCancelled.title")
        ).build()
      ).queue()
    }
  }
}