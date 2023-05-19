package xyz.pokecord.bot.core.structures.store.packages

import xyz.pokecord.bot.core.managers.database.models.User
import xyz.pokecord.bot.core.structures.discord.Bot

object GemsPackage : Package() {
  override val id = "gems"
  override val items: List<Item> = listOf(
    GemItem(
      "yBkHEDpYVm",
      1,
      100
    ),
    GemItem(
      "5f-1qvXVmv",
      5,
      525
    ),
    GemItem(
      "UqUpJ9S1t-",
      10,
      1100
    ),
    GemItem(
      "BlW-2_tDWS",
      15,
      1680
    ),
    GemItem(
      "lUGG9CUqd0",
      25,
      3000
    ),
    GemItem(
      "WqJ86786Uf",
      50,
      6750
    )
  )

  override suspend fun giveReward(bot: Bot, userData: User, item: Item): Boolean {
    if (item !is GemItem) return false
    bot.database.userRepository.incGems(userData, item.gems)
    return true
  }

  class GemItem(
    id: String,
    price: Number,
    val gems: Int
  ) : Item(id, price)
}
