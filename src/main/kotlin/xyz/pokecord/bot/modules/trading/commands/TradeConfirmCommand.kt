package xyz.pokecord.bot.modules.trading.commands

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object TradeConfirmCommand: Command() {
  override val name = "confirm"

  @Executor
  suspend fun execute(context: ICommandContext) {
    if (!context.hasStarted(true)) return

    val tradeState = context.getTradeState()
    if (tradeState == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.commands.add.errors.notInTrade")
        ).build()
      ).queue()
      return
    }

    if(
      tradeState.initiator.credits == 0 &&
      tradeState.initiator.pokemon.isEmpty() &&
      tradeState.receiver.credits == 0 &&
      tradeState.receiver.pokemon.isEmpty()
    ) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.commands.confirm.errors.emptyTrade")
        ).build()
      ).queue()
      return
    }

    val partnerTradeState = if(tradeState.initiator.userId == context.author.id) tradeState.receiver else tradeState.initiator
    if(partnerTradeState.confirmed) {
      context.bot.database.tradeRepository.deleteTrade(tradeState)

      val authorTradeData = if(tradeState.initiator.userId == context.author.id) tradeState.initiator else tradeState.receiver
      val partnerTradeData = if(tradeState.initiator.userId == context.author.id) tradeState.receiver else tradeState.initiator

      val authorUserData = context.bot.database.userRepository.getUser(authorTradeData.userId)
      val partnerUserData = context.bot.database.userRepository.getUser(partnerTradeData.userId)

      val session = context.bot.database.startSession()

      session.use {
        session.startTransaction()

        if(authorTradeData.credits > 0) {
          context.bot.database.userRepository.incCredits(partnerUserData, authorTradeData.credits, session)
        }

        if(partnerTradeData.credits > 0) {
          context.bot.database.userRepository.incCredits(authorUserData, partnerTradeData.credits, session)
        }

        val authorPokemon = context.bot.database.pokemonRepository.getPokemonByIds(authorTradeData.pokemon)
        val partnerPokemon = context.bot.database.pokemonRepository.getPokemonByIds(partnerTradeData.pokemon)

        if(authorTradeData.pokemon.isNotEmpty()) {
          for(pokemon in authorPokemon) {
            val (leveledUp, evolved) = context.bot.database.pokemonRepository.levelUpAndEvolveIfPossible(
              pokemon, null, null, partnerPokemon.map { it.id }.toMutableList()
            )

            context.bot.database.pokemonRepository.tradeTransfer(pokemon, partnerUserData.id)
            context.bot.database.userRepository.tradeCountUpdate(pokemon, authorUserData, partnerUserData)

          }
        }

        if(partnerTradeData.pokemon.isNotEmpty()) {
          for(pokemon in partnerPokemon) {
            val (leveledUp, evolved) = context.bot.database.pokemonRepository.levelUpAndEvolveIfPossible(
              pokemon, null, null, authorPokemon.map { it.id }.toMutableList()
            )

            context.bot.database.pokemonRepository.tradeTransfer(pokemon, authorUserData.id)
            context.bot.database.userRepository.tradeCountUpdate(pokemon, partnerUserData, authorUserData)
          }
        }

        session.commitTransactionAndAwait()
      }

      context.reply(
        context.embedTemplates
          .normal(
            "testing"
          )
          .addField("Trainer", authorUserData.tag, true)
          .addField("Credits", context.translator.numberFormat(authorTradeData.credits), true)
          .addField("Pokemon", "test", true)
          .addField("Trainer", partnerUserData.tag, true)
          .addField("Credits", context.translator.numberFormat(partnerTradeData.credits), true)
          .addField("Pokemon", "test", true)
          .build()
      ).queue()
    } else {
      context.bot.database.tradeRepository.confirm(tradeState, context.author.id)

      context.reply(
        context.embedTemplates.normal(
          context.translate("modules.trading.commands.confirm.embeds.partConfirmed.description"),
          context.translate("modules.trading.commands.confirm.embeds.partConfirmed.title")
        ).build()
      ).queue()
    }
  }
}