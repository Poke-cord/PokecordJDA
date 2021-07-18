package xyz.pokecord.bot.modules.pokemon.commands

import kotlinx.coroutines.launch
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.PokemonOrder

class OrderCommand : Command() {
  override val name = "order"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(
      optional = true
    ) order: PokemonOrder?
  ) {
    val effectiveOrder = order ?: PokemonOrder.POKEDEX
    context.reply(
      context.embedTemplates.normal(
        context.translate("modules.pokemon.commands.order.ordering")
      ).build()
    ).queue()
    context.bot.backgroundCoroutineScope.launch {
      module.bot.database.pokemonRepository.reindexPokemon(context.author.id, effectiveOrder)
      context.reply(
        context.embedTemplates.normal(
          context.translate("modules.pokemon.commands.order.ordered")
        ).build()
      ).queue()
    }
  }
}
