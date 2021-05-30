package xyz.pokecord.bot.core.structures.store.packages

import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext

object GemsPackage : Package() {
  override val id = "gems"
  override val items: List<Item> = listOf(
    GemItem(
      "100_gems",
      0.99,
      100
    ),
    GemItem(
      "525_gems",
      4.99,
      525
    ),
    GemItem(
      "1100_gems",
      9.99,
      1100
    ),
    GemItem(
      "1680_gems",
      14.99,
      1680
    ),
    GemItem(
      "3000_gems",
      24.99,
      3000
    ),
    GemItem(
      "6750_gems",
      49.99,
      6750
    )
  )

  override suspend fun giveReward(context: MessageReceivedContext, item: Item) {
    if (item !is GemItem) return
    context.bot.database.userRepository.incGems(context.getUserData(), item.gems)
  }

  class GemItem(
    id: String,
    price: Number,
    val gems: Int
  ) : Item(id, price)
}
