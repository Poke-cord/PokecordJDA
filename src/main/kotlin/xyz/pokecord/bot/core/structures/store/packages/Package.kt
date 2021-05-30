package xyz.pokecord.bot.core.structures.store.packages

import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext

abstract class Package {
  abstract val id: String
  abstract val items: List<Item>

  abstract suspend fun giveReward(context: MessageReceivedContext, item: Item)

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
