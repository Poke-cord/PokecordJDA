package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext

class UnusableItem(id: Int) : Item(id) {
  override suspend fun use(context: MessageReceivedContext, args: List<String>): UsageResult {
    return UsageResult(
      false,
      context.embedTemplates.error(
        context.translate("items.unusable.description"),
        context.translate("items.unusable.title")
      )
    )
  }
}
