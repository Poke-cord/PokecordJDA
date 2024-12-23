package xyz.pokecord.bot.modules.trade.commands

import org.litote.kmongo.coroutine.abortTransactionAndAwait
import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.modules.trade.TradeModule

object TradeConfirmCommand : Command() {
  override val name = "confirm"
  override var aliases = arrayOf("cf")

  @Executor
  suspend fun execute(context: ICommandContext) {
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

    if (
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

    val partnerTradeState =
      if (tradeState.initiator.userId == context.author.id) tradeState.receiver else tradeState.initiator
    if (partnerTradeState.confirmed) {
      context.bot.database.tradeRepository.endTrade(tradeState)

      val authorTradeData =
        if (tradeState.initiator.userId == context.author.id) tradeState.initiator else tradeState.receiver
      val partnerTradeData =
        if (tradeState.initiator.userId == context.author.id) tradeState.receiver else tradeState.initiator

      val session = context.bot.database.startSession()

      session.use {
        session.startTransaction()

        val authorUserData = context.bot.database.userRepository.getUser(authorTradeData.userId)
        val partnerUserData = context.bot.database.userRepository.getUser(partnerTradeData.userId)

        if (authorTradeData.credits > 0) {
          if (!context.bot.database.userRepository.incCredits(partnerUserData, authorTradeData.credits, session)) {
            session.abortTransactionAndAwait()
            context.reply(
              context.embedTemplates.normal(
                context.translate(
                  "misc.embeds.transactionCancelled.description",
                  mapOf(
                    "type" to "trade confirm"
                  )
                ),
                context.translate("misc.embeds.transactionCancelled.title")
              ).build()
            ).queue()
            return
          }
        }

        if (partnerTradeData.credits > 0) {
          if (!context.bot.database.userRepository.incCredits(authorUserData, partnerTradeData.credits, session)) {
            session.abortTransactionAndAwait()
            context.reply(
              context.embedTemplates.normal(
                context.translate(
                  "misc.embeds.transactionCancelled.description",
                  mapOf(
                    "type" to "trade confirm"
                  )
                ),
                context.translate("misc.embeds.transactionCancelled.title")
              ).build()
            ).queue()
            return
          }
        }

        val authorPokemon = context.bot.database.pokemonRepository.getPokemonByIds(authorTradeData.pokemon)
        val partnerPokemon = context.bot.database.pokemonRepository.getPokemonByIds(partnerTradeData.pokemon)

        val authorPokemonText =
          TradeModule.getTradeStatePokemonText(context, authorPokemon, partnerPokemon.map { it.id }, true, session)
        authorPokemon.map { pokemon ->
          context.bot.database.pokemonRepository.updateOwnerId(pokemon, partnerUserData.id, session)
          context.bot.database.userRepository.addDexCatchEntry(authorUserData, pokemon, session)
        }

        val partnerPokemonText =
          TradeModule.getTradeStatePokemonText(context, partnerPokemon, authorPokemon.map { it.id }, true, session)
        partnerPokemon.map { pokemon ->
          context.bot.database.pokemonRepository.updateOwnerId(pokemon, authorUserData.id, session)
          context.bot.database.userRepository.addDexCatchEntry(partnerUserData, pokemon, session)
        }

        context.bot.database.userRepository.incPokemonCount(
          authorUserData,
          partnerPokemon.size - authorPokemon.size,
          session
        )
        context.bot.database.userRepository.incPokemonCount(
          partnerUserData,
          authorPokemon.size - partnerPokemon.size,
          session
        )

        session.commitTransactionAndAwait()

        context.reply(
          context.embedTemplates.success(
              context.translate(
                "modules.trading.commands.confirm.embeds.confirmed.description",
                mapOf(
                  "author" to "<@${authorUserData.id}>",
                  "partner" to "<@${partnerUserData.id}>"
                )
              ),
              context.translate("modules.trading.commands.confirm.embeds.confirmed.title"),
            )
            .addField("Trainer", authorUserData.tag, true)
            .addField("Credits", context.translator.numberFormat(authorTradeData.credits), true)
            .addField("Pokemon", authorPokemonText.joinToString("\n").ifEmpty { "None" }, true)
            .addField("Trainer", partnerUserData.tag, true)
            .addField("Credits", context.translator.numberFormat(partnerTradeData.credits), true)
            .addField("Pokemon", partnerPokemonText.joinToString("\n").ifEmpty { "None" }, true)
            .build()
        ).queue()
      }
    } else {
      context.bot.database.tradeRepository.confirm(tradeState, context.author.id)

      context.reply(
        context.embedTemplates.confirmation(
          context.translate("modules.trading.commands.confirm.embeds.partConfirmed.description"),
          context.translate("modules.trading.commands.confirm.embeds.partConfirmed.title")
        ).build()
      ).queue()
    }
  }
}