package xyz.pokecord.bot.modules.pokemon.commands

import kotlinx.coroutines.launch
import org.litote.kmongo.coroutine.commitTransactionAndAwait
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
    val userData = context.getUserData()
    if (userData.pokemonCount > MAX_POKEMON_COUNT) {
      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.pokemon.commands.order.tooManyPokemon",
            mapOf(
              "maxPokemonCount" to context.translator.numberFormat(MAX_POKEMON_COUNT)
            )
          )
        ).build()
      ).queue()
      return
    }
    val effectiveOrder = order ?: PokemonOrder.POKEDEX
    context.reply(
      context.embedTemplates.normal(
        context.translate("modules.pokemon.commands.order.ordering")
      ).build()
    ).queue()
    context.bot.backgroundCoroutineScope.launch {
      val session = module.bot.database.startSession()
      session.use {
        it.startTransaction()
        module.bot.database.pokemonRepository.reindexPokemon(it, context.author.id, effectiveOrder)
        module.bot.database.userRepository.resetNextIndex(it, userData)
        it.commitTransactionAndAwait()
      }
      context.reply(
        context.embedTemplates.normal(
          context.translate("modules.pokemon.commands.order.ordered")
        ).build()
      ).queue()
    }
  }

  companion object {
    private const val MAX_POKEMON_COUNT = 20000
  }
}
