package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.api.ICommandContext

class EVItem(id: Int, val type: String) : Item(id) {

  val vitamins = listOf("HP Up", "Protein", "Iron", "Calcium", "Zinc", "Carbos")

  override suspend fun use(context: ICommandContext, args: List<String>): UsageResult {
    val userData = context.getUserData()
    val pokemon = context.bot.database.pokemonRepository.getPokemonById(userData.selected!!)
      ?: return UsageResult(false, context.embedTemplates.start())

    context.bot.database.pokemonRepository.levelUpAndEvolveIfPossible(pokemon, gainedXp = pokemon.requiredXpToLevelUp())

    return UsageResult(
      true,
      context.embedTemplates.normal(
        context.translate(
          "items.rareCandy.embed.description",
          mapOf(
            "pokemon" to context.translator.pokemonDisplayName(pokemon),
            "level" to pokemon.level.toString()
          )
        ),
        context.translate("items.rareCandy.embed.title")
      )
    )

  }

  enum class EVItems(
    val id: Int,
    val identifier: String,
    val itemName: String,
    val cost: Int = 0,
    val flingPower: Int = 0,
    val flingEffectId: Int = 0,
    val useGems: Boolean = false
  ) {
    HPUp(10200000, "hp-up", "HP Up"),
    Protein(10200001, "protein", "Protein"),
    Iron(10200010, "iron", "Iron"),
    Calcium(10200011, "calcium", "Calcium"),
    Zinc(10200100, "zinc", "Zinc"),
    Carbos(10200101, "carbos", "Carbos")
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