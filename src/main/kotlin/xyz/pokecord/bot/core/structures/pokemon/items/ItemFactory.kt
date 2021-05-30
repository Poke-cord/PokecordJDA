package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.core.structures.pokemon.ItemData

object ItemFactory {
  val items: MutableMap<Int, Item> = mutableMapOf()

  init {
//    items.putAll(CreditsItem.creditsMap)
    items.putAll(RedeemItem.redeemMap)

    items.putAll(
      ItemData.getByCategoryId(EvolutionItem.categoryId).map {
        it.id to EvolutionItem(it.id)
      }
    )

    items.putAll(
      ItemData.getByCategoryId(MachineItem.categoryId).map {
        it.id to MachineItem(it.id)
      }
    )

    items[ShinyBoostItem.id] = ShinyBoostItem
  }
}
