package xyz.pokecord.bot.modules.economy.commands

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.core.structures.discord.Command
import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext
import xyz.pokecord.bot.core.structures.pokemon.ItemData
import kotlin.math.roundToInt

class BuyCommand : Command() {
  override val name = "Buy"

  @Executor
  suspend fun execute(
    context: MessageReceivedContext,
    @Argument(optional = true) amount: Int?,
    @Argument(consumeRest = true) itemName: String?,
  ) {
    if (!context.hasStarted(true)) return

    if (itemName == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.economy.commands.buy.errors.noItemName")
        ).build()
      ).queue()
      return
    }

    val itemData = ItemData.getByName(itemName)
    if (itemData == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.economy.commands.buy.errors.itemNotFound")
        ).build()
      ).queue()
      return
    }

    val effectiveAmount = amount ?: 1

    if (effectiveAmount < 1 || effectiveAmount >= 1024) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.economy.commands.buy.errors.invalidAmount")
        ).build()
      ).queue()
      return
    }

    val userData = context.getUserData()
    val session = module.bot.database.startSession()

    var cost = itemData.cost * effectiveAmount

    if (itemData.usesGems && userData.gems < cost) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.economy.commands.buy.errors.notEnoughGems")
        ).build()
      ).queue()
      return
    } else if (itemData.usesTokens && userData.tokens < cost) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.economy.commands.buy.errors.notEnoughTokens")
        ).build()
      ).queue()
      return
    } else {
      cost = (cost * userData.getShopDiscount()).roundToInt()
      if (userData.credits < cost) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.economy.commands.buy.errors.notEnoughCredits")
          ).build()
        ).queue()
        return
      }
    }

    session.use {
      it.startTransaction()
      module.bot.database.userRepository.addInventoryItem(context.author.id, itemData.id, effectiveAmount, session)
      when {
        itemData.usesGems -> module.bot.database.userRepository.incGems(userData, -itemData.cost, it)
        itemData.usesTokens -> module.bot.database.userRepository.incTokens(userData, -itemData.cost, it)
        else -> module.bot.database.userRepository.incCredits(userData, -cost, it)
      }
      it.commitTransactionAndAwait()
    }

    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.economy.commands.buy.embed.description",
          mapOf(
            "amount" to effectiveAmount.toString(),
            "item" to itemData.name
          )
        ),
        context.translate("modules.economy.commands.buy.embed.title")
      ).build()
    ).queue()
  }
}
