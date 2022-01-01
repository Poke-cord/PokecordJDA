package xyz.pokecord.bot.modules.battle.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.MoveData
import xyz.pokecord.bot.core.structures.pokemon.PokemonMove
import xyz.pokecord.bot.utils.Confirmation
import xyz.pokecord.bot.utils.PokemonResolvable

object TeachCommand : Command() {
  override val name = "Teach"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(
      name = "pokemon",
      optional = true,
      aliases = ["p"],
      prefixed = true
    ) pokemonResolvable: PokemonResolvable?,
    @Argument slot: Int?,
    @Argument(consumeRest = true) move: String?,
  ) {
    if (!context.hasStarted(true)) return

    val selfCurrentBattle = context.bot.database.battleRepository.getUserCurrentBattle(context.author)
    if (selfCurrentBattle != null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.battle.commands.moves.teach.errors.inBattle")
        ).build()
      ).queue()
      return
    }

    val pokemon = context.resolvePokemon(context.author, context.getUserData(), pokemonResolvable)
    if (pokemon == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.battle.commands.moves.errors.pokemonNotFound")
        ).build()
      ).queue()
      return
    }

    if (slot == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.battle.commands.moves.teach.errors.slotNotSpecified")
        ).build()
      ).queue()
      return
    }

    if (slot < 1 || slot > 4) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.battle.commands.moves.teach.errors.slotOutOfRange")
        ).build()
      ).queue()
      return
    }

    if (move == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.battle.commands.moves.teach.errors.moveNotSpecified")
        ).build()
      ).queue()
      return
    }

    val availableMoves = PokemonMove.getById(pokemon.id)?.mapNotNull {
      if (it.moveMethodId == 1 && it.requiredLevel <= pokemon.level) it
      else null
    } ?: emptyList()

    val targetMove = move.toIntOrNull()?.let { availableMoves.getOrNull(it - 1) } ?: availableMoves.find {
      it.moveData.name.equals(
        move.toString(),
        ignoreCase = true
      ) || it.moveData.identifier.equals(move.toString(), ignoreCase = true)
    }
    if (targetMove == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.battle.commands.moves.teach.errors.moveNotFound")
        ).build()
      ).queue()
      return
    }

    if (pokemon.moves[slot - 1] != 0) {
      val confirmation = Confirmation(context)
      val confirmed = confirmation.result(
        context.embedTemplates.confirmation(
          context.translate(
            "modules.battle.commands.moves.teach.replaceConfirmation.description",
            mapOf(
              "slot" to slot.toString(),
              "move" to MoveData.getById(pokemon.moves[slot - 1])!!.name
            )
          ),
          context.translate("modules.battle.commands.moves.teach.replaceConfirmation.title"),
        ),
        mentionRepliedUser = true
      )
      if (!confirmed) {
        val embed = context.embedTemplates.normal(
          "",
          context.translate("modules.battle.commands.moves.teach.errors.cancelled"),
        ).setFooter(null).build()

        confirmation.sentMessage?.editMessageEmbeds(embed)?.queue() ?: context.reply(embed).queue()
        return
      }
    }

    context.bot.database.pokemonRepository.teachMove(pokemon, slot, targetMove.id)

    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.battle.commands.moves.teach.embed.description",
          "move" to targetMove.moveData.name,
          "pokemon" to context.translator.pokemonDisplayName(pokemon),
          "slot" to slot.toString()
        ),
        context.translate("modules.battle.commands.moves.teach.embed.title")
      )
        .build()
    ).queue()
  }
}