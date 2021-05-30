package xyz.pokecord.bot.modules.pokemon.commands

import xyz.pokecord.bot.core.structures.discord.Command
import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext
import xyz.pokecord.bot.core.structures.pokemon.Pokemon

class PickCommand : Command() {
  override val name = "Pick"

  override var rateLimit = 5000L

  @Executor
  suspend fun execute(
    context: MessageReceivedContext,
    @Argument(name = "Pokémon Name") pokemonName: String?
  ) {
    if (context.hasStarted()) {
      context.reply(
        context.embedTemplates.error(context.translate("modules.pokemon.commands.pick.pickingTwice")).build()
      ).queue()
    } else {
      if (pokemonName == null) {
        context.reply(
          context.embedTemplates.error(context.translate("modules.pokemon.commands.pick.noNameProvided")).build()
        ).queue()
        return
      }

      val pokemon = Pokemon.getByName(pokemonName)
      if (pokemon == null || !Pokemon.starters.contains(pokemon.id)) {
        context.reply(
          context.embedTemplates.error(
            context.translate(
              "modules.pokemon.commands.pick.starterNotFound",
              "prefix" to context.getPrefix()
            )
          ).build()
        ).queue()
        return
      }

      val ownedPokemon = module.bot.database.userRepository.givePokemon(context.getUserData(), pokemon.id, true)

      val translatedPokemonName =
        context.translator.pokemonDisplayName(ownedPokemon)

      context.reply(
        context.embedTemplates.normal(
          context.translate("modules.pokemon.commands.pick.embed.description", "pokemon" to translatedPokemonName),
          "$translatedPokemonName | ${
            context.translate(
              "misc.texts.starterPokemon"
            )
          }"
        )
          .addField(context.translate("misc.texts.name"), translatedPokemonName, false)
          .addField(context.translate("misc.texts.evolution"), context.translator.evolution(pokemon), true)
          .addField(
            context.translate("misc.texts.types"),
            pokemon.types.joinToString(", ") { it.name?.name ?: "Unknown" },
            true
          )
          .addField(context.translate("misc.texts.height"), "${pokemon.height / 10.0} m", true)
          .addField(
            context.translate("misc.texts.habitat"),
            context.translator.habitat(pokemon)?.name ?: "Unknown",
            true
          )
          .addField(context.translate("misc.texts.gender"), context.translator.gender(ownedPokemon), true)
          .addField(context.translate("misc.texts.weight"), "${pokemon.weight / 10.0} kg", true)
          .setThumbnail(ownedPokemon.imageUrl)
          .setColor(pokemon.species.color.colorCode)
          .build()
      ).queue()
    }
  }
}
