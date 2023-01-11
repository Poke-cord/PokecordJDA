package xyz.pokecord.bot.core.structures.store.packages

import xyz.pokecord.bot.core.managers.database.models.User
import xyz.pokecord.bot.core.structures.discord.Bot

abstract class Package {
  abstract val id: String
  abstract val items: List<Item>

  abstract suspend fun giveReward(bot: Bot, userData: User, item: Item): Boolean

  open class Item(
    val id: String,
    val price: Number
  )

  companion object {
    val packages: List<Package> = listOf(
      GemsPackage,
      RolesPackage
    )
  }
}
