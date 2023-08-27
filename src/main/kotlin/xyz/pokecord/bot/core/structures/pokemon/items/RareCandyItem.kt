package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.api.ICommandContext

object RareCandyItem : Item(50, false) {
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

    val count = args.firstOrNull()?.toIntOrNull() ?: 1

    val inventoryItem = context.bot.database.userRepository.getInventoryItem(context.author.id, data.id)

    if (inventoryItem == null || inventoryItem.amount < count) {
      return UsageResult(
        false,
        context.embedTemplates.error(
          context.translate(
            "modules.profile.commands.item.errors.itemNotOwned.description",
            mapOf(
              "user" to context.author.asMention,
              "item" to data.name
            )
          ),
          context.translate(
            "modules.profile.commands.item.errors.itemNotOwned.title"
          )
        )
      )
    }


    repeat(count) {
      val result = context.bot.database.pokemonRepository.levelUpAndEvolveIfPossible(pokemon, gainedXp = pokemon.requiredXpToLevelUp())

      if (!result.first) {
        return@repeat
      }

      // Consume
      context.bot.database.userRepository.consumeInventoryItem(inventoryItem)
    }

    return UsageResult(
      false,
      context.embedTemplates.normal(
        context.translate("items.rareCandy.embed.description",
          mapOf(
            "pokemon" to context.translator.pokemonDisplayName(pokemon),
            "level" to pokemon.level.toString(),
            "user" to context.author.asMention
          )
        ),
        context.translate("items.rareCandy.embed.title")
      )
    )
  }
}
