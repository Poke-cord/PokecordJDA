package xyz.pokecord.bot.modules.pokepedia.commands

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
        context.embedTemplates.error(context.translate("misc.errors.pokemonNotFound"))
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
    val baseStats = "**${context.translate("misc.texts.baseStats")}**"

    context.reply(
      context.embedTemplates.normal(
        suggestedMovesHeader + "\n" + moves.joinToString("\n") + "\n\n" + baseStats + "\n"  +
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
        "Pokémon Movesets │ ${context.translator.pokemonName(pokemon)}"
      )
        .setThumbnail(pokemon.imageUrl)
        .setFooter(context.translate("modules.pokemon.commands.moveset.embed.footer"))
        .setColor(pokemon.species.color.colorCode)
        .build()
    ).queue()
  }
}
