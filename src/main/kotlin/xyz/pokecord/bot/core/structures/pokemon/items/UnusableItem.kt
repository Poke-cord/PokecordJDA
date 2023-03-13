package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.api.ICommandContext

open class UnusableItem(id: Int) : Item(id) {
  override suspend fun use(context: ICommandContext, args: List<String>): UsageResult {
    return UsageResult(
      false,
      context.embedTemplates.error(
        context.translate("items.unusable.description"),
        context.translate("items.unusable.title")
      )
    )
  }
}
