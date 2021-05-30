package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext
import xyz.pokecord.bot.core.structures.pokemon.ItemData

object ShinyBoostItem : Item(10010000) {
  private const val categoryId = 1001
  val itemData = ItemData(id, "shiny-boost", "Shiny Boost", categoryId, 999999999, 0, 0, usesTokens = true)

  override suspend fun use(context: MessageReceivedContext, args: List<String>): UsageResult {
    val userData = context.getUserData()
    context.bot.database.userRepository.incShinyRate(userData, -10)
    return UsageResult(
      true,
      context.embedTemplates.normal(
        context.translate("items.shinyBoost.embed.description"),
        context.translate("items.shinyBoost.embed.title")
      )
    )
  }
}
