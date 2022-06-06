package xyz.pokecord.bot.modules.release.commands

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.PokemonResolvable

object ReleaseRemoveCommand : Command() {
  override val name: String = "remove"
  override var aliases = arrayOf("r")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument pokemon: PokemonResolvable?
  ) {
    if (!context.hasStarted(true)) return

    val releaseState = context.getTradeState()
    if (releaseState == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.pokemon.commands.release.errors.notInRelease")
        ).build()
      ).queue()
      return
    }

    if (pokemon == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("misc.errors.pokemonNotFound")
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
            "modules.trading.commands.remove.errors.noPokemonFound",
            "index" to pokemon.toString()
          )
        ).build()
      ).queue()
    } else {
      val session = context.bot.database.startSession()
      session.use {
        session.startTransaction()
        context.bot.database.tradeRepository.removePokemon(releaseState, context.author.id, selectedPokemon._id, session)
        context.bot.database.tradeRepository.clearConfirmState(releaseState, session)
        session.commitTransactionAndAwait()
      }

      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.pokemon.commands.release.embeds.center.removePokemon.description",
            "pokemon" to context.translator.pokemonName(selectedPokemon).toString()
          ),
          context.translate("modules.pokemon.commands.release.embeds.center.removePokemon.title")
        ).build()
      ).queue()
    }
  }
}