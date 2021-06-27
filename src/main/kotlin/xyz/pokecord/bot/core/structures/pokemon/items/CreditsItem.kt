package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.api.ICommandContext

class CreditsItem(id: Int, val credits: Int) : Item(id) {
  override suspend fun use(context: ICommandContext, args: List<String>): UsageResult {
    val creditsItemData = CreditsItems.values().find { it.id == id }!!
    val userData = context.getUserData()
    context.bot.database.userRepository.incCredits(userData, creditsItemData.credits)
    return UsageResult(
      true, context.embedTemplates.normal(
        context.translate(
          "items.credits.embed.description",
          "credits" to context.translator.numberFormat(creditsItemData.credits)
        ),
        context.translate("items.credits.embed.title")
      )
    )
  }

  enum class CreditsItems(
    val id: Int,
    val identifier: String,
    val itemName: String,
    val cost: Int,
    val credits: Int,
    val flingPower: Int = 0,
    val flingEffectId: Int = 0,
    val useGems: Boolean = true
  ) {
    Credits80(10020000, "80-credits", "80 Credits Package", 1, 80),
    Credits800(10020001, "800-credits", "800 Credits Package", 10, 800),
    Credits8000(10020002, "8000-credits", "8000 Credits Package", 100, 8000),
    Credits80000(10020003, "80000-credits", "80000 Credits Package", 1000, 80000)
  }

  companion object {
    private fun CreditsItem.asPair(): Pair<Int, CreditsItem> {
      return id to this
    }

    val creditsMap: MutableMap<Int, Item> = mutableMapOf(
      CreditsItem(CreditsItems.Credits80.id, CreditsItems.Credits80.credits).asPair(),
      CreditsItem(CreditsItems.Credits800.id, CreditsItems.Credits800.credits).asPair(),
      CreditsItem(CreditsItems.Credits8000.id, CreditsItems.Credits8000.credits).asPair(),
      CreditsItem(CreditsItems.Credits80000.id, CreditsItems.Credits80000.credits).asPair(),
    )

    const val categoryId = 1002
  }
}
