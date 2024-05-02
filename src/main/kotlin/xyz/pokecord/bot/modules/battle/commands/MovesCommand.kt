package xyz.pokecord.bot.modules.battle.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.core.structures.pokemon.MoveData
import xyz.pokecord.bot.core.structures.pokemon.PokemonMove
import xyz.pokecord.bot.utils.PokemonResolvable

object MovesCommand : ParentCommand() {
  override val name = "Moves"
  override val childCommands = mutableListOf<Command>(TeachCommand)

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(name = "pokemon") pokemonResolvable: PokemonResolvable?
  ) {
    if (!context.hasStarted(true)) return

    val pokemon = context.resolvePokemon(context.author, context.getUserData(), pokemonResolvable)
    if (pokemon == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("misc.errors.pokemonDoesNotExist")
        ).build()
      )
      return
    }

    val availableMoves = PokemonMove.getById(pokemon.id)?.mapNotNull {
      if (it.moveMethodId == 1 && it.requiredLevel <= pokemon.level) it.id
      else null
    } ?: emptyList()

    context.reply(
      context.embedTemplates.menu(
        context.translate("modules.battle.embeds.moves.base.description"),
        context.translate("modules.battle.embeds.moves.base.title",
          mapOf(
            "level" to pokemon.level.toString(),
            "pokemon" to context.translator.pokemonDisplayName(pokemon),
            "speciesId" to pokemon.data.formattedSpeciesId
          )
        ),
      )
        .setThumbnail(pokemon.imageUrl)
        .addField(
          context.translate("modules.battle.texts.availableMoves"),
          availableMoves.map {
            if (it == 0) {
              "- ${context.translate("modules.battle.texts.noMove")}"
            } else {
              "- ${MoveData.getById(it)?.name ?: context.translate("modules.battle.texts.unknownMove")}"
            }
          }.joinToString("\n").ifEmpty { context.translate("modules.battle.texts.noMoves") },
          true
        )
        .addField(
          context.translate("modules.battle.texts.learnedMoves"),
          pokemon.moves.map {
            if (it == 0) {
              "- ${context.translate("modules.battle.texts.noMove")}"
            } else {
              "- ${MoveData.getById(it)?.name ?: context.translate("modules.battle.texts.unknownMove")}"
            }
          }.joinToString("\n").ifEmpty { context.translate("modules.battle.texts.noMoves") },
          true
        )
        .build()
    ).queue()
  }
}
