package xyz.pokecord.bot.modules.pokemon.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.core.structures.pokemon.Stat

class PokedexCommand : Command() {
  override val name = "Pokedex"

  override var aliases = arrayOf("d", "dex")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(prefixed = true, aliases = ["sh"]) shiny: Boolean?,
    @Argument(name = "pokemon", consumeRest = true) pokemonNameOrId: String?
  ) {
    if (pokemonNameOrId == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.pokemon.commands.pokedex.noPokemonName")
        ).build()
      ).queue()
      return
    }

    val id = pokemonNameOrId.toIntOrNull()
    val pokemon = if (id != null) Pokemon.getById(id) else Pokemon.getByName(pokemonNameOrId)

    if (pokemon == null) {
      context.reply(
        context.embedTemplates.error(context.translate("modules.pokemon.commands.pokedex.pokemonNotFound")).build()
      ).queue()
      return
    }

    val genderRate =
      if (pokemon.species.genderRate == -1) context.translate("misc.texts.genderless")
      else "${100 - pokemon.species.genderRate * 12.5}% ${context.translate("misc.texts.male")}, ${pokemon.species.genderRate * 12.5}% ${
        context.translate(
          "misc.texts.female"
        )
      }"

    val nextEvolution = context.translator.evolution(pokemon)
    val prevEvolution = context.translator.prevEvolution(pokemon)

    val userData = context.getUserData()

    val embed = context.embedTemplates
      .empty()
      .setTitle(
        context.translate(
          "modules.pokemon.commands.pokedex.embed.title",
          mapOf(
            "speciesId" to pokemon.formattedSpeciesId,
            "pokemon" to "${context.translator.pokemonName(pokemon)}${if (shiny == true) " ⭐" else ""}"
          )
        )
      )
      .setColor(pokemon.species.color.colorCode)
      .setImage(Pokemon.getImageUrl(pokemon.id, shiny == true))
      .setFooter(
        if ((if (shiny == true) userData.caughtShinies else userData.caughtPokemon).contains(pokemon.id)) context.translate(
          "modules.pokemon.commands.pokedex.footers.caught"
        )
        else context.translate("modules.pokemon.commands.pokedex.footers.uncaught")
      )
      .addField(
        context.translate("misc.texts.type"),
        pokemon.types.mapNotNull { it.name?.name }.joinToString(", "),
        true
      ).addField(
        context.translate("misc.texts.height"),
        "${pokemon.height / 10.0} m",
        true
      ).addField(
        context.translate("misc.texts.weight"),
        "${pokemon.weight / 10.0} kg",
        true
      )
      .addField(context.translate("misc.texts.prevEvolution"), prevEvolution, true)
      .addField(context.translate("misc.texts.nextEvolution"), nextEvolution, true)
      .addField(
        context.translate("misc.texts.habitat"),
        context.translator.habitat(pokemon)?.name ?: context.translate("misc.texts.unknown"),
        true
      )
      .addField(context.translate("misc.texts.genderRate"), genderRate, true)
      .addField(context.translate("misc.texts.genus"), context.translator.pokemonGenus(pokemon), true)
      .addField(context.translate("misc.texts.generation"), pokemon.species.romanGenerationId, true)
      .addField(
        context.translate("misc.texts.baseStats"),
        """
          **${context.translator.stat(Stat.hp)}** ${Stat.hp.getBaseValue(pokemon.id).toString()}
          **${context.translator.stat(Stat.attack)}** ${Stat.attack.getBaseValue(pokemon.id).toString()}
          **${context.translator.stat(Stat.defense)}** ${Stat.defense.getBaseValue(pokemon.id).toString()}
          **${context.translator.stat(Stat.specialAttack)}** ${Stat.specialAttack.getBaseValue(pokemon.id).toString()}
          **${context.translator.stat(Stat.specialDefense)}** ${
          Stat.specialDefense.getBaseValue(pokemon.id).toString()
        }
          **${context.translator.stat(Stat.speed)}** ${Stat.speed.getBaseValue(pokemon.id).toString()}
        """.trimIndent(),
        true
      )
      .addField(
        context.translate("misc.texts.altNames"),
        pokemon.species.getNames().map { it.name }.toSet().joinToString(", "),
        true
      )
    context.reply(embed.build()).queue()
  }
}
