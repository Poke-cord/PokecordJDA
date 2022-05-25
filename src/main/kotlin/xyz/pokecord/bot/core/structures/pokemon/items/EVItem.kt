package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.utils.PokemonStats

class EVItem(id: Int, val type: String) : Item(id) {

  override suspend fun use(context: ICommandContext, args: List<String>): UsageResult {
    val evItemData = EVItems.values().find { it.id == id }!!
    val userData = context.getUserData()
    val pokemon = context.bot.database.pokemonRepository.getPokemonById(userData.selected!!)
      ?: return UsageResult(false, context.embedTemplates.start())

    // Meet the requirement of in total 510 effort points in total per Pokémon
    if (pokemon.evs.total >= 510) {
      return UsageResult(
        false,
        context.embedTemplates.error(
          context.translate(
            "items.ev.errors.alreadyMaxEv",
            "pokemon" to context.translator.pokemonDisplayName(pokemon)
          )
        )
      )
    }

    context.bot.database.pokemonRepository.addEffort(pokemon, evItemData);

    return UsageResult(
      true,
      context.embedTemplates.normal(
        context.translate(
          "items.ev.embed.description",
          mapOf(
            "pokemon" to context.translator.pokemonDisplayName(pokemon),
            "statName" to evItemData.statName
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
    val stat: PokemonStats,
    val statName: String,
    val cost: Int = 0,
    val flingPower: Int = 0,
    val flingEffectId: Int = 0,
    val useGems: Boolean = false,
  ) {
    HPUp(10200000, "hp-up", "HP Up", PokemonStats(0, 0, 10,
      0, 0, 0), "HP"),
    Protein(10200001, "protein", "Protein", PokemonStats(10, 0, 0,
      0, 0, 0), "Attack"),
    Iron(10200010, "iron", "Iron", PokemonStats(0, 10, 0,
      0, 0, 0), "Defense"),
    Calcium(10200011, "calcium", "Calcium", PokemonStats(0, 0, 0,
      10, 0, 0), "Special Attack"),
    Zinc(10200100, "zinc", "Zinc", PokemonStats(0, 0, 0,
      0, 10, 0), "Special Defense"),
    Carbos(10200101, "carbos", "Carbos", PokemonStats(0, 0, 0,
      0, 0, 10), "Speed")
  }

  companion object {
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