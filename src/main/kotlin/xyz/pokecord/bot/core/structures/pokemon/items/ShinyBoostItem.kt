package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.api.ICommandContext

object ShinyBoostItem : Item(10010000) {
  const val categoryId = 1001

  override suspend fun use(context: ICommandContext, args: List<String>): UsageResult {
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
