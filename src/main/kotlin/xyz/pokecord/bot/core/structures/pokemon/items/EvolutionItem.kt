package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.EmbedTemplates

class EvolutionItem(
  id: Int
) : Item(id) {
  override suspend fun use(context: ICommandContext, args: List<String>): UsageResult {
    val userData = context.getUserData()
    val selectedPokemon = context.bot.database.pokemonRepository.getPokemonById(userData.selected!!)!!

    val oldPokemonName = context.translator.pokemonName(selectedPokemon)!!

    val (_, evolved) = context.bot.database.pokemonRepository.levelUpAndEvolveIfPossible(
      selectedPokemon,
      id
    )

    if (!evolved) {
      return UsageResult(
        false,
        context.embedTemplates.error(context.translate("modules.profile.commands.item.errors.invalidTarget"))
      )
    }

    return UsageResult(
      true,
      context.embedTemplates.normal(
        context.translate(
          "items.evolution.embed.description",
          mapOf(
            "user" to context.author.asMention,
            "pokemon" to oldPokemonName,
            "evolvedPokemon" to context.translator.pokemonName(selectedPokemon)!!
          )
        ),
        context.translate("items.evolution.embed.title")
      ).setColor(EmbedTemplates.Color.GREEN.code)
    )
  }

  companion object {
    const val categoryId = 10
  }
}
