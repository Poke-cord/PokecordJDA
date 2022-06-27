package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.utils.PokemonStats

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

    val result = context.bot.database.pokemonRepository.addEffort(pokemon, evItemData.id)

    // If a EV stat is already maxed out
    if (result) {
      return UsageResult(
        false,
        context.embedTemplates.error(
          context.translate(
            "items.ev.errors.alreadyMaxStat",
            "pokemon" to context.translator.pokemonDisplayName(pokemon),
            "stat" to evsMap[evItemData.id].toString()
          )
        )
      )
    }


    return UsageResult(
      true,
      context.embedTemplates.normal(
        context.translate(
          "items.ev.embed.description",
          mapOf(
            "pokemon" to context.translator.pokemonDisplayName(pokemon),
            "statName" to evItemData.statName,
            "points" to points.toString()
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
    val statName: String,
    var cost: Int = 0,
    val flingPower: Int = 0,
    val flingEffectId: Int = 0,
    val useGems: Boolean = false,
  ) {
    HPUp(45, "hp-up", "HP Up", "HP"),
    Protein(46, "protein", "Protein", "Attack"),
    Iron(47, "iron", "Iron", "Defense"),
    Calcium(49, "calcium", "Calcium", "Special Attack"),
    Zinc(52, "zinc", "Zinc", "Special Defense"),
    Carbos(48, "carbos", "Carbos", "Speed")
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
      val evs = listOf(EVItems.HPUp, EVItems.Protein,
        EVItems.Iron, EVItems.Calcium, EVItems.Zinc, EVItems.Carbos)

      return evs.random();
    }

    const val categoryId = 1020
  }

}