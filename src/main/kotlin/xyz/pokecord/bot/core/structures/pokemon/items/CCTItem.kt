package xyz.pokecord.bot.core.structures.pokemon.items

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext

// Credit Conversion Token
object CCTItem : Item(10010001) {
  const val categoryId = 1001

  private const val usageCost = 100_000

  override suspend fun use(context: ICommandContext, args: List<String>): UsageResult {
    val tokenCount = args.firstOrNull()?.toIntOrNull() ?: 1

    val userData = context.getUserData()
    if (userData.credits < (usageCost * tokenCount)) {
      return UsageResult(
        false,
        context.embedTemplates.error(
          context.translate(
            "items.cct.errors.notEnoughCredits",
            "cost" to context.translator.numberFormat(usageCost * tokenCount)
          )
        )
      )
    }

    val inventoryItem = context.bot.database.userRepository.getInventoryItem(context.author.id, id)

    if (inventoryItem == null || inventoryItem.amount < tokenCount) {
      return UsageResult(
        false,
        context.embedTemplates.error(
          context.translate(
            "items.cct.errors.notEnoughCCT"
          )
        )
      )
    }

    val session = context.bot.database.startSession()
    session.use {
      it.startTransaction()
      context.bot.database.userRepository.incTokens(userData, tokenCount, it)
      context.bot.database.userRepository.incCredits(userData, -(usageCost * tokenCount), it)
      context.bot.database.userRepository.consumeInventoryItem(inventoryItem, tokenCount, it)
      it.commitTransactionAndAwait()
    }

    return UsageResult(
      false, // don't consume because we already consumed above inside the transaction session
      context.embedTemplates.normal(
        context.translate(
          "items.cct.embed.description", mapOf(
            "cost" to context.translator.numberFormat(usageCost * tokenCount),
            "token" to context.translator.numberFormat(tokenCount)
          )
        ),
        context.translate("items.cct.embed.title")
      )
    )
  }
}
