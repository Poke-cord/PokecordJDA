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

        val authorPokemonText = authorPokemon.map { pokemon ->
          val initialName = context.translator.pokemonName(pokemon)
          val (_, evolved) = context.bot.database.pokemonRepository.levelUpAndEvolveIfPossible(
            pokemon, null, null, partnerPokemon.map { it.id }.toMutableList()
          )

          context.bot.database.pokemonRepository.tradeTransfer(pokemon, partnerUserData.id, session)
          context.bot.database.userRepository.tradeCountUpdate(pokemon, authorUserData, partnerUserData)

          val evolutionNameText = if(evolved) "-> ${context.translator.pokemonName(pokemon)}" else ""
          "${pokemon.index} | ${initialName}${evolutionNameText} - ${pokemon.level} - ${pokemon.ivPercentage}"
        }

        val partnerPokemonText = partnerPokemon.map { pokemon ->
          val initialName = context.translator.pokemonName(pokemon)
          val (_, evolved) = context.bot.database.pokemonRepository.levelUpAndEvolveIfPossible(
            pokemon, null, null, authorPokemon.map { it.id }.toMutableList()
          )

          context.bot.database.pokemonRepository.tradeTransfer(pokemon, authorUserData.id, session)
          context.bot.database.userRepository.tradeCountUpdate(pokemon, partnerUserData, authorUserData)

          val evolutionNameText = if(evolved) "-> ${context.translator.pokemonName(pokemon)}" else ""
          "${pokemon.index} | ${initialName}${evolutionNameText} - ${pokemon.level} - ${pokemon.ivPercentage}"
        }

        context.bot.database.userRepository.incPokemonCount(authorUserData, partnerPokemon.size - authorPokemon.size, session)
        context.bot.database.userRepository.incPokemonCount(partnerUserData, authorPokemon.size - partnerPokemon.size, session)

        session.commitTransactionAndAwait()

        context.reply(
          context.embedTemplates
            .normal(
              context.translate("module.trading.commands.confirm.embeds.confirmed.description"),
              context.translate("module.trading.commands.confirm.embeds.confirmed.title"),
            )
            .addField("Trainer", authorUserData.tag, true)
            .addField("Credits", context.translator.numberFormat(authorTradeData.credits), true)
            .addField("Pokemon", authorPokemonText.joinToString { "\n" }, true)
            .addField("Trainer", partnerUserData.tag, true)
            .addField("Credits", context.translator.numberFormat(partnerTradeData.credits), true)
            .addField("Pokemon", partnerPokemonText.joinToString { "\n" }, true)
            .build()
        ).queue()
      }
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