package xyz.pokecord.bot.modules.pokepedia.commands

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
          context.translate("misc.errors.missingArguments.noPokemonName")
        ).build()
      ).queue()
      return
    }

    val id = pokemonNameOrId.toIntOrNull()
    val pokemon = if (id != null) Pokemon.getById(id) else Pokemon.getByName(pokemonNameOrId)

    if (pokemon == null) {
      context.reply(
        context.embedTemplates.error(context.translate("misc.errors.pokemonNotFound")).build()
      ).queue()
      return
    }

    if (!pokemon.hasShiny) {
      context.reply(
        context.embedTemplates.error(context.translate("modules.pokemon.commands.pokedex.errors.noShiny")).build()
      ).queue()
      return
    }

    val genderRate =
      if (pokemon.species.genderRate == -1) context.translate("misc.texts.genderless")
      else "${100 - pokemon.species.genderRate * 12.5}% ${context.translate("misc.texts.male")}, ${pokemon.species.genderRate * 12.5}% ${
        context.translate("misc.texts.female")
      }"

    val nextEvolution = context.translator.evolution(pokemon)
    val prevEvolution = context.translator.prevEvolution(pokemon)

    val userData = context.getUserData()
    val language = context.getLanguage()

    val embed = context.embedTemplates
      .empty()
      .setTitle(
        context.translate(
          "modules.pokemon.commands.pokedex.embed.title",
          mapOf(
            "speciesId" to pokemon.formattedSpeciesId,
            "pokemon" to "${context.translator.pokemonName(pokemon)}${pokemon.getEmoji(shiny == true)}"
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
        context.translate("misc.texts.appearance"),
        "**Height**: ${pokemon.height / 10.0} m\n**Weight**: ${pokemon.weight / 10.0} kg",
        true
      ).addField(
        context.translate("misc.texts.obtained"),
        "Catchable",
        true
        //Define each Pokemon as catchable, redeemable etc. and make this dynamic.
      )
      .addField(context.translate("misc.texts.prevEvolution"), prevEvolution, true)
      .addField(context.translate("misc.texts.nextEvolution"), nextEvolution, true)
      //Add level that the Pokemon evolves at.
      .addField(context.translate("misc.texts.genderRate"), genderRate, true)
      //.addField(
      //  context.translate("misc.texts.habitat"),
      //  context.translator.habitat(pokemon)?.name ?: context.translate("misc.texts.unknown"),
      //  true
      //)
      //.addField(context.translate("misc.texts.genus"), context.translator.pokemonGenus(pokemon), true)
      //.addField(context.translate("misc.texts.generation"), pokemon.species.romanGenerationId, true)
      .addField(
        context.translate("misc.texts.baseStats"),
        """
          `${context.translate("misc.texts.hp").padEnd(7)}| ${Stat.hp.getBaseValue(pokemon.id).toString().padEnd(4)}`
          `${context.translate("misc.texts.attack").padEnd(7)}| ${
          Stat.attack.getBaseValue(pokemon.id).toString().padEnd(4)
        }`
          `${context.translate("misc.texts.defense").padEnd(7)}| ${
          Stat.defense.getBaseValue(pokemon.id).toString().padEnd(4)
        }`
          `${context.translate("misc.texts.specialAttack").padEnd(7)}| ${
          Stat.specialAttack.getBaseValue(pokemon.id).toString().padEnd(4)
        }`
          `${context.translate("misc.texts.specialDefense").padEnd(7)}| ${
          Stat.specialDefense.getBaseValue(pokemon.id).toString().padEnd(4)
        }`
          `${context.translate("misc.texts.speed").padEnd(7)}| ${
          Stat.speed.getBaseValue(pokemon.id).toString().padEnd(4)
        }`
        """.trimIndent(),
        true
      )
      .addField(
        context.translate("misc.texts.altNames"),
        pokemon.species.getNames().map { it.name }.toSet().joinToString("\n"),
        true
      )
      .addField(
        context.translate("misc.texts.formNames"),
        pokemon.species.forms.map { form ->
          form.names.filter {
            it.languageId == language.pokeApiLanguageId
          }
        }
          .flatten()
          .map {
            it.pokemonName
          }
          .toSet()
          .joinToString(", "),
        true
      )
    context.reply(embed.build()).queue()
  }
}
