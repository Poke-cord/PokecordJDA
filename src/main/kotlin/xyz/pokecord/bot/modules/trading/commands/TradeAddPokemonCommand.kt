package xyz.pokecord.bot.modules.trading.commands

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
// import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.modules.staff.StaffCommand
import xyz.pokecord.bot.utils.PokemonResolvable

object TradeAddPokemonCommand : StaffCommand() {
  override val name = "pokemon"
  override var aliases = arrayOf("p", "pkmn", "pokemon", "poke")

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
          context.translate("modules.trading.commands.add.errors.notInTrade")
        ).build()
      ).queue()
    } else {
      if (pokemon == null) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.trading.commands.add.errors.noNumberPokemon")
          ).build()
        ).queue()
        return
      }

      val userData = context.getUserData()
      val selectedPokemon = context.resolvePokemon(context.author, userData, pokemon)

      if (selectedPokemon == null) {
        context.reply(
          context.embedTemplates.error(
            context.translate(
              "modules.trading.commands.add.errors.noPokemonFound"
            )
          ).build()
        ).queue()
      } else {
        val authorTradeState =
          if (tradeState.initiator.userId == context.author.id) tradeState.initiator else tradeState.receiver
        if (authorTradeState.pokemon.size >= 20) {
          context.reply(
            context.embedTemplates.error(
              context.translate("modules.trading.commands.add.errors.maxPokemonCount")
            ).build()
          ).queue()
          return
        }

        val transfer = selectedPokemon.transferable(context.bot.database)
        if (transfer != OwnedPokemon.TransferStates.SUCCESS) {
          context.reply(
            context.embedTemplates.error(
              transfer.errMessage,
              context.translate("modules.trading.commands.add.errors.notTransferableTitle")
            ).build()
          ).queue()
          return
        }

        val session = context.bot.database.startSession()
        session.use {
          session.startTransaction()
          context.bot.database.tradeRepository.addPokemon(tradeState, context.author.id, selectedPokemon._id, session)
          context.bot.database.tradeRepository.clearConfirmState(tradeState, session)
          session.commitTransactionAndAwait()
        }

        context.reply(
          context.embedTemplates.normal(
            context.translate(
              "modules.trading.commands.add.embeds.addPokemon.description",
              "pokemon" to context.translator.pokemonName(selectedPokemon).toString()
            ),
            context.translate("modules.trading.commands.add.embeds.addPokemon.title")
          ).build()
        ).queue()
      }
    }
  }
}