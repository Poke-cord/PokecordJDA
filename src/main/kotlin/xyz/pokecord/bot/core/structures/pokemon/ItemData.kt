package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.core.structures.pokemon.items.*
import xyz.pokecord.bot.utils.Json
import kotlin.system.exitProcess

@Serializable
data class ItemData(
  val id: Int,
  val identifier: String,
  val name: String,
  val categoryId: Int,
  var cost: Int,
  val flingPower: Int,
  val flingEffectId: Int,
  @Transient val usesGems: Boolean = false,
  @Transient var usesTokens: Boolean = false
) {
  companion object {
    var items: MutableList<ItemData>

    private val disabledCategoryIds = arrayOf(8, 11, 21, 32, 33, 34, 36, 39, 40, 41)
    private val disabledItemIds = arrayOf<Int>()
    private val unusableItemIds = arrayOf(206)

    init {
      val stream = ItemData::class.java.getResourceAsStream("/data/items.json")
      if (stream == null) {
        println("Item data not found. Exiting...")
        exitProcess(0)
      }
      val json = stream.readAllBytes().decodeToString()
      items = Json.decodeFromString(json)

      addAllCustomItems()
      applyCustomModifications()

      items = items.filter { !disabledItemIds.contains(it.id) && !disabledCategoryIds.contains(it.categoryId) }
        .toMutableList()

      for (unusableItemId in unusableItemIds) {
        ItemFactory.items[unusableItemId] = UnusableItem(unusableItemId)
      }
    }

    fun getByCategoryId(categoryId: Int) = items.filter { it.categoryId == categoryId }
    fun getById(id: Int) = items.find { it.id == id }

    fun getByName(name: String): ItemData? {
      return items.find {
        name.startsWith(it.name, true) || name.startsWith(it.identifier, true) || name.replace(
          ' ',
          '-'
        ).startsWith(it.identifier, true)
      }
    }

    private fun addAllCustomItems() {
      // Credits
      items.addAll(
        CreditsItem.CreditsItems.values().map {
          ItemData(
            it.id,
            it.identifier,
            it.itemName,
            CreditsItem.categoryId,
            it.cost,
            it.flingPower,
            it.flingEffectId,
            it.useGems
          )
        }
      )

      // Redeems
      items.addAll(
        RedeemItem.Redeems.values().map {
          ItemData(
            it.id,
            it.identifier,
            it.itemName,
            RedeemItem.categoryId,
            it.cost,
            it.flingPower,
            it.flingEffectId,
            it.useGems
          )
        }
      )

      // Glimmering Candy
      items.add(
        ItemData(
          GlimmeringCandyItem.id,
          "glimmering-candy",
          "Glimmering Candy",
          GlimmeringCandyItem.categoryId,
          3,
          0,
          0,
          usesTokens = true
        )
      )
      // Credit Conversion Token
      items.add(
        ItemData(
          CCTItem.id,
          "cct",
          "Credit Conversion Token",
          CCTItem.categoryId,
          0,
          0,
          0
        )
      )
    }

    private fun applyCustomModifications() {
      items.forEach { itemData ->
        when (itemData.id) {
          50 -> {
            itemData.usesTokens = true
            itemData.cost = 1
          }
        }
      }
    }
  }
}
