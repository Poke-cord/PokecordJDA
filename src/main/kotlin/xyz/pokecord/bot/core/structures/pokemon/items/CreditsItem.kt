package xyz.pokecord.bot.core.structures.pokemon.items

//
//class CreditsItem(id: Int, val credits: Int) : Item(id) {
//  override suspend fun use(context: MessageReceivedContext, args: List<String>): UsageResult {
//    TODO("Not yet implemented")
//  }
//
//  enum class CreditsItems(
//    val id: Int,
//    val identifier: String,
//    val itemName: String,
//    val cost: Int,
//    val credits: Int,
//    val flingPower: Int = 0,
//    val flingEffectId: Int = 0,
//    val useGems: Boolean = true
//  ) {
//    Credits80(10020000, "80-credits", "80 Credits", 1, 80),
//    Credits800(10020001, "800-credits", "800 Credits", 10, 800),
//    Credits8000(10020002, "8000-credits", "8000 Credits", 100, 8000),
//    Credits80000(10020003, "80000-credits", "80000 Credits", 1000, 80000)
//  }
//
//  companion object {
//    private fun CreditsItem.asPair(): Pair<Int, CreditsItem> {
//      return id to this
//    }
//
//    val creditsMap: MutableMap<Int, Item> = mutableMapOf(
//      CreditsItem(CreditsItems.Credits80.id, CreditsItems.Credits80.credits).asPair(),
//      CreditsItem(CreditsItems.Credits800.id, CreditsItems.Credits800.credits).asPair(),
//      CreditsItem(CreditsItems.Credits8000.id, CreditsItems.Credits8000.credits).asPair(),
//      CreditsItem(CreditsItems.Credits80000.id, CreditsItems.Credits80000.credits).asPair(),
//    )
//
//    const val categoryId = 1002
//  }
//}
