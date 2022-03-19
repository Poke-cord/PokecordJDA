package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.pokemon.Nature

class NatureMintItem(
  id: Int
) : Item(id) {
  override suspend fun use(context: ICommandContext, args: List<String>): UsageResult {
    val targetNature = Nature.getByIdentifier(data.identifier.removeSuffix("-mint"))!!

    val currentSelfBattle = context.bot.database.battleRepository.getUserCurrentBattle(context.author)
    if (currentSelfBattle != null) {
      return UsageResult(
        false,
        context.embedTemplates.error(context.translate("items.mints.errors.inBattle"))
      )
    }

    val userData = context.getUserData()
    val selectedPokemon = context.bot.database.pokemonRepository.getPokemonById(userData.selected!!)!!

    if (targetNature.identifier.equals(selectedPokemon.nature, true)) {
      return UsageResult(
        false,
        context.embedTemplates.error(
          context.translate("items.mints.errors.invalidTarget")
        )
      )
    }

    context.bot.database.pokemonRepository.updateNature(selectedPokemon, targetNature.name!!.name)
    return UsageResult(
      true,
      context.embedTemplates.normal(
        context.translate(
          "items.mints.embed.description",
          mapOf(
            "pokemon" to context.translator.pokemonDisplayName(selectedPokemon),
            "nature" to targetNature.name.name
          )
        ),
        context.translate("items.mints.embed.title"),
      )
    )
  }

  companion object {
    const val categoryId = 50
  }
}
