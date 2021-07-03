package xyz.pokecord.bot.modules.pokemon.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.MoveData
import xyz.pokecord.bot.core.structures.pokemon.Moveset
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.core.structures.pokemon.Stat

class MovesetCommand : Command() {
  override val name = "Moveset"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(name = "pokemon") pokemonName: String?
  ) {
    if (pokemonName == null) {
      context.reply(
        context.embedTemplates.error(context.translate("modules.pokemon.commands.moveset.errors.noPokemonName")).build()
      ).queue()
      return
    }

    val pokemon = Pokemon.getByName(pokemonName)

    if (pokemon == null) {
      context.reply(
        context.embedTemplates.error(context.translate("modules.pokemon.commands.moveset.errors.noPokemonFound"))
          .build()
      ).queue()
      return
    }

    val moveset = Moveset.getByPokemonId(pokemon.id)
    if (moveset == null) {
      context.reply(
        context.embedTemplates.error(context.translate("modules.pokemon.commands.moveset.errors.noMoveset"))
          .build()
      ).queue()
      return
    }

    val moves = moveset.moves.mapNotNull {
      val moveData = MoveData.getById(it) ?: return@mapNotNull null
      "- ${moveData.name}"
    }

    val suggestedMovesHeader = "**${context.translate("misc.texts.suggestedMoves")}**"

    context.reply(
      context.embedTemplates.normal(
        suggestedMovesHeader + "\n" + moves.joinToString("\n") + "\n\n" +
            """
          **${context.translate("misc.texts.baseStats")}**
          **${context.translator.stat(Stat.hp)}**: ${Stat.hp.getBaseValue(pokemon.id)}
          **${context.translator.stat(Stat.attack)}**: ${Stat.attack.getBaseValue(pokemon.id)}
          **${context.translator.stat(Stat.defense)}**: ${Stat.defense.getBaseValue(pokemon.id)}
          **${context.translator.stat(Stat.specialAttack)}**: ${Stat.specialAttack.getBaseValue(pokemon.id)}
          **${context.translator.stat(Stat.specialDefense)}**: ${Stat.specialDefense.getBaseValue(pokemon.id)}
          **${context.translator.stat(Stat.speed)}**: ${Stat.speed.getBaseValue(pokemon.id)}
        """.trimIndent(),
        "${pokemon.formattedSpeciesId} | ${context.translator.pokemonName(pokemon)} | Moveset"
      )
        .setThumbnail(pokemon.imageUrl)
        .setFooter(context.translate("modules.pokemon.commands.moveset.embed.footer"))
        .setColor(pokemon.species.color.colorCode)
        .build()
    ).queue()
  }
}
