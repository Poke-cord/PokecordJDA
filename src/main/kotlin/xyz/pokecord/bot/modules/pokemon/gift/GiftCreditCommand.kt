package xyz.pokecord.bot.modules.pokemon.gift

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.entities.User
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.modules.pokemon.commands.GiftCommand
import xyz.pokecord.bot.core.structures.discord.base.Command
// import xyz.pokecord.bot.modules.staff.StaffCommand
import xyz.pokecord.bot.utils.Confirmation

object GiftCreditCommand : Command() {
  override val name = "Credit"

  override var aliases = arrayOf("c")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument receiver: User?,
    @Argument amount: Int?
  ) {
    if (!context.hasStarted(true)) return

    if (context.getTradeState() != null || context.getBattleState() != null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("misc.errors.inState")
        ).build()
      ).queue()
      return
    }

    if (receiver == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.pokemon.commands.gift.errors.mentionUser")
        ).build()
      ).queue()
      return
    }
    if (receiver.isBot) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.pokemon.commands.gift.errors.botReceiver")
        ).build()
      ).queue()
      return
    }
    if (receiver.id == context.author.id) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.pokemon.commands.gift.errors.selfReceiver")
        ).build()
      ).queue()
      return
    }

    val receiverData = module.bot.database.userRepository.getUser(receiver)
    if (receiverData.selected == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("misc.errors.userHasNotStarted")
        ).build()
      ).queue()
      return
    }

    if (!GiftCommand.receivingGifts(context, receiverData)) return

    if (amount == null || amount <= 0) {
      context.reply(
        context.embedTemplates.error(
          context.translate("misc.errors.invalidAmount")
        ).build()
      ).queue()
      return
    }

    var userData = context.getUserData()
    if (userData.credits < amount) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.pokemon.commands.gift.errors.notEnoughCredits")
        ).build()
      ).queue()
      return
    }

    val confirmation = Confirmation(context)
    val confirmed = confirmation.result(
      context.embedTemplates.confirmation(
        context.translate(
          "modules.pokemon.commands.gift.embeds.confirmation.credits",
          mapOf(
            "sender" to context.author.asMention,
            "receiver" to receiver.asMention,
            "amount" to context.translator.numberFormat(amount)
          )
        ),
        context.translate("modules.pokemon.commands.gift.embeds.confirmation.title")
      )
    )

    if (confirmed) {
      userData = context.getUserData()
      if (userData.credits < amount) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.pokemon.commands.gift.errors.notEnoughCredits")
          ).build()
        ).queue()
        return
      }

      if (!module.bot.database.userRepository.giftCredits(userData, receiverData, amount)) {
        context.reply(
          context.embedTemplates.error(
            context.translate(
              "misc.embeds.transactionCancelled.description",
              mapOf(
                "type" to "gift credit"
              )
            ),
            context.translate("misc.embeds.transactionCancelled.title")
          ).build()
        ).queue()
        return
      }

      try {
        val privateChannel = receiver.openPrivateChannel().await()
        privateChannel.sendMessageEmbeds(
          context.embedTemplates.normal(
            context.translate(
              "modules.pokemon.commands.gift.embeds.giftReceived.credits",
              mapOf(
                "sender" to context.author.asMention,
                "amount" to context.translator.numberFormat(amount)
              )
            ),
            context.translate("modules.pokemon.commands.gift.embeds.giftReceived.title")
          ).build()
        ).await()
      } catch (_: Exception) {
      }

      confirmation.sentMessage!!.editMessageEmbeds(
        context.embedTemplates.normal(
          context.translate(
            "modules.pokemon.commands.gift.embeds.giftSent.credits",
            mapOf(
              "sender" to context.author.asMention,
              "receiver" to receiver.asMention,
              "amount" to context.translator.numberFormat(amount)
            )
          ),
          context.translate("modules.pokemon.commands.gift.embeds.giftSent.title")
        ).build()
      ).queue()
    } else {
      confirmation.sentMessage!!.editMessageEmbeds(
        context.embedTemplates.normal(
          context.translate("modules.pokemon.commands.gift.embeds.cancelled.description"),
          context.translate("modules.pokemon.commands.gift.embeds.cancelled.title")
        ).build()
      ).queue()
    }
  }
}
