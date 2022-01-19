package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.pokemon.Machine
import xyz.pokecord.bot.core.structures.pokemon.MoveData
import xyz.pokecord.bot.core.structures.pokemon.MoveMethod

class MachineItem(
  id: Int
) : Item(id) {
  override suspend fun use(context: ICommandContext, args: List<String>): UsageResult {
    val currentSelfBattle = context.bot.database.battleRepository.getUserCurrentBattle(context.author)
    if (currentSelfBattle != null) {
      return UsageResult(
        false,
        context.embedTemplates.error(context.translate("items.machine.errors.inBattle"))
      )
    }

    val machine = Machine.getByItemId(id)
      ?: return UsageResult(
        false,
        context.embedTemplates.error(
          context.translate("items.machine.errors.unknownMachine")
        )
      )

    if (args.isEmpty()) {
      return UsageResult(
        false,
        context.embedTemplates.error(
          context.translate("items.machine.errors.noSlotSpecified")
        )
      )
    }

    val slot = args.first().toIntOrNull()

    if (slot == null || slot < 1 || slot > 4) {
      return UsageResult(
        false,
        context.embedTemplates.error(
          context.translate("items.machine.errors.invalidSlotSpecified")
        )
      )
    }

    val userData = context.getUserData()
    val selectedPokemon = context.bot.database.pokemonRepository.getPokemonById(userData.selected!!)!!
    val move =
      selectedPokemon.data.moves.find { it.id == machine.moveId && it.moveMethodId == MoveMethod.Machine.id && it.requiredLevel <= selectedPokemon.level }
        ?: return UsageResult(
          false,
          context.embedTemplates.error(
            context.translate("items.machine.errors.invalidTarget")
          )
        )

    context.bot.database.pokemonRepository.teachMove(selectedPokemon, slot, move.id)
    return UsageResult(
      true,
      context.embedTemplates.normal(
        context.translate(
          "misc.texts.moveLearned",
          mapOf(
            "pokemon" to context.translator.pokemonDisplayName(selectedPokemon),
            "move" to (MoveData.getById(move.id)?.name ?: "N/A"),
            "user" to context.author.asMention
          )
        ),
        context.translate("items.machine.embed.title")
      )
    )
  }

  companion object {
    const val categoryId = 37
  }
}
