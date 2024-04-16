package xyz.pokecord.bot.modules.trade.commands

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.PokemonResolvable

object TradeRemovePokemonCommand : Command() {
  override val name = "pokemon"
  override var aliases = arrayOf("p", "pkmn", "pokemon", "poke", "pk")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument pokemon: PokemonResolvable?
  ) {
    if (!context.hasStarted(true)) return

    val tradeState = context.getTradeState()
    if (tradeState == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.errors.notInTrade")
        ).build()
      ).queue()
      return
    }

    if (pokemon == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.commands.remove.errors.noNumberPokemon")
        ).build()
      ).queue()
      return
    }

    val userData = context.getUserData()
    val selectedPokemon = context.resolvePokemon(context.author, userData, pokemon)

    if (selectedPokemon == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("misc.errors.pokemonNotFound")
        ).build()
      ).queue()
    } else {
      val transfer = selectedPokemon.transferable(context.bot.database)
      if (transfer != OwnedPokemon.TransferStates.TRADE_SESSION) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.trading.commands.remove.errors.notAdded")
          ).build()
        ).queue()
        return
      }

      val session = context.bot.database.startSession()
      session.use {
        session.startTransaction()
        context.bot.database.tradeRepository.removePokemon(tradeState, context.author.id, selectedPokemon._id, session)
        context.bot.database.tradeRepository.clearConfirmState(tradeState, session)
        session.commitTransactionAndAwait()
      }

      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.trading.commands.remove.embeds.removePokemon.description",
            "pokemon" to context.translator.pokemonName(selectedPokemon).toString()
          ),
          context.translate("modules.trading.commands.remove.embeds.title")
        ).build()
      ).queue()
    }
  }
}