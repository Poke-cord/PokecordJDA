package xyz.pokecord.bot.modules.economy.commands

import org.litote.kmongo.coroutine.abortTransactionAndAwait
import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.ItemData
import xyz.pokecord.bot.core.structures.pokemon.items.ItemFactory
import kotlin.math.roundToInt

class BuyCommand : Command() {
  override val name = "Buy"

  @Executor
  suspend fun execute(
    context: ICommandContext,
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

    var effectiveAmount = amount ?: 1

    val itemData = ItemData.getByName(itemName) ?: ItemData.getByName("$amount $itemName")?.also { effectiveAmount = 1 }
    if (itemData == null || !ItemFactory.items.contains(itemData.id)) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.economy.commands.buy.errors.itemNotFound")
        ).build()
      ).queue()
      return
    }

    if (itemData.cost <= 0) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.economy.commands.buy.errors.cannotBePurchased")
        ).build()
      ).queue()
      return
    }

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

    if (itemData.usesGems) {
      if (userData.gems < cost) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.economy.commands.buy.errors.notEnoughGems")
          ).build()
        ).queue()
        return
      }
    } else if (itemData.usesTokens) {
      if (userData.tokens < cost) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.economy.commands.buy.errors.notEnoughTokens")
          ).build()
        ).queue()
        return
      }
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

    val cancelled = session.use {
      it.startTransaction()
      module.bot.database.userRepository.addInventoryItem(context.author.id, itemData.id, effectiveAmount, session)
      when {
        itemData.usesGems -> module.bot.database.userRepository.incGems(userData, -cost, it)
        itemData.usesTokens -> module.bot.database.userRepository.incTokens(userData, -cost, it)
        else -> {
          if (!module.bot.database.userRepository.incCredits(userData, -cost, it)) {
            it.abortTransactionAndAwait()
            return@use true
          }
        }
      }
      it.commitTransactionAndAwait()
      false
    }

    if (cancelled) {
      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "misc.embeds.transactionCancelled.description",
            mapOf(
              "type" to "buy"
            )
          ),
          context.translate("misc.embeds.transactionCancelled.title")
        ).build()
      ).queue()
      return
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
