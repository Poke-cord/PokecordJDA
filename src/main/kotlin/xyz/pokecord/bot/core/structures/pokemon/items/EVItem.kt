package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.pokemon.Stat

class EVItem(id: Int, val type: String) : Item(id) {

  override suspend fun use(context: ICommandContext, args: List<String>): UsageResult {
    val evItemData = EVItems.values().find { it.id == id }!!
    val userData = context.getUserData()
    val pokemon = context.bot.database.pokemonRepository.getPokemonById(userData.selected!!)
      ?: return UsageResult(false, context.embedTemplates.start())

    // Meet the requirement of total 510 effort points in total per Pokémon
    if (pokemon.evs.total >= 510) {
      return UsageResult(
        false,
        context.embedTemplates.error(
          context.translate(
            "items.ev.errors.alreadyMaxTotalEv",
            "pokemon" to context.translator.pokemonDisplayName(pokemon)
          )
        )
      )
    }

    val count = args.firstOrNull()?.toIntOrNull() ?: 1

    val inventoryItem = context.bot.database.userRepository.getInventoryItem(context.author.id, evItemData.id)

    if (inventoryItem == null || inventoryItem.amount < count) {
      return UsageResult(
        false,
        context.embedTemplates.error(
          context.translate(
            "modules.profile.commands.item.errors.itemNotOwned.description",
            mapOf(
              "user" to context.author.asMention,
              "item" to evItemData.name
            )
          ),
          context.translate(
            "modules.profile.commands.item.errors.itemNotOwned.title"
          )
        )
      )
    }

    var consumed = 0
    repeat(count) {
      val result = context.bot.database.pokemonRepository.addEffort(pokemon, evItemData.stat)

      // If a EV stat is already maxed out
      if (result) {
        UsageResult(
          false,
          context.embedTemplates.error(
            context.translate(
              "items.ev.errors.alreadyMaxStat",
              "pokemon" to context.translator.pokemonDisplayName(pokemon),
              "stat" to evsMap[evItemData.id].toString()
            )
          )
        )
        return@repeat
      }

      // Consume
      context.bot.database.userRepository.consumeInventoryItem(inventoryItem)

      consumed++
    }

    return UsageResult(
      false,
      context.embedTemplates.normal(
        context.translate(
          "items.ev.embed.description",
          mapOf(
            "pokemon" to context.translator.pokemonDisplayName(pokemon),
            "statName" to context.translator.stat(evItemData.stat),
            "points" to (points * consumed).toString()
          )
        ),
        context.translate(
          "items.ev.embed.title",
          mapOf(
            "vitamin" to evItemData.itemName
          )
        )
      )
    )

  }

  enum class EVItems(
    val id: Int,
    val identifier: String,
    val itemName: String,
    val stat: Stat,
    var cost: Int = 0,
    val flingPower: Int = 0,
    val flingEffectId: Int = 0,
    val useGems: Boolean = false,
  ) {
    HPUp(45, "hp-up", "HP Up", Stat.hp),
    Protein(46, "protein", "Protein", Stat.attack),
    Iron(47, "iron", "Iron", Stat.defense),
    Calcium(49, "calcium", "Calcium", Stat.specialAttack),
    Zinc(52, "zinc", "Zinc", Stat.specialDefense),
    Carbos(48, "carbos", "Carbos", Stat.speed)
  }

  companion object {
    const val points: Int = 1

    private fun EVItem.asPair(): Pair<Int, EVItem> {
      return id to this
    }

    val evsMap: MutableMap<Int, Item> = mutableMapOf(
      EVItem(EVItems.HPUp.id, EVItems.HPUp.itemName).asPair(),
      EVItem(EVItems.Protein.id, EVItems.Protein.itemName).asPair(),
      EVItem(EVItems.Iron.id, EVItems.Iron.itemName).asPair(),
      EVItem(EVItems.Calcium.id, EVItems.Calcium.itemName).asPair(),
      EVItem(EVItems.Zinc.id, EVItems.Zinc.itemName).asPair(),
      EVItem(EVItems.Carbos.id, EVItems.Carbos.itemName).asPair(),
    )

    fun getRandom(): EVItems {
      val evs = listOf(
        EVItems.HPUp, EVItems.Protein,
        EVItems.Iron, EVItems.Calcium, EVItems.Zinc, EVItems.Carbos
      )

      return evs.random()
    }

    const val categoryId = 1020
  }

}