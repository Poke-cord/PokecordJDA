package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.api.ICommandContext

object RareCandyItem : Item(50) {
  override suspend fun use(context: ICommandContext, args: List<String>): UsageResult {
    val userData = context.getUserData()
    val pokemon = context.bot.database.pokemonRepository.getPokemonById(userData.selected!!)
      ?: return UsageResult(false, context.embedTemplates.start())
    if (pokemon.level >= 100) {
      return UsageResult(
        false,
        context.embedTemplates.error(
          context.translate(
            "items.rareCandy.errors.alreadyMaxLevel",
            "pokemon" to context.translator.pokemonDisplayName(pokemon)
          )
        )
      )
    }
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
}
