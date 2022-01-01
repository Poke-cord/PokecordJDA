package xyz.pokecord.bot.modules.trading.commands

import dev.minn.jda.ktx.await
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.modules.trading.TradingModule
import xyz.pokecord.bot.utils.Confirmation

object TradeStatusCommand: Command() {
  override val name = "status"

  @Executor
  suspend fun execute(
    context: ICommandContext
  ) {
    if (!context.hasStarted(true)) return

    val tradeState = context.getTradeState()
    if(tradeState == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.commands.status.errors.notInTrade")
        ).build()
      ).queue()
      return
    }

    val partner = if(tradeState.initiator.userId == context.author.id) tradeState.receiver else tradeState.initiator
    val initiator = if(tradeState.initiator.userId == context.author.id) tradeState.initiator else tradeState.receiver

    val partnerUser = context.jda.retrieveUserById(partner.userId).await()
    if(partnerUser == null) {
      val confirmation = Confirmation(context, initiator.userId)
      val confirmed = confirmation.result(
        context.embedTemplates.confirmation(
          context.translate("modules.trading.commands.status.errors.noPartnerFound.description"),
          context.translate("modules.trading.commands.status.errors.noPartnerFound.title")
        )
      )

      if(confirmed) {
        context.bot.database.tradeRepository.deleteTrade(tradeState)
        context.reply(
          context.embedTemplates.normal(
            context.translate("modules.trading.commands.status.embeds.tradeEnded.description"),
            context.translate("modules.trading.commands.status.embeds.tradeEnded.title")
          ).build()
        ).queue()
      }
      return
    }

    val authorTradeData = if(tradeState.initiator.userId == context.author.id) tradeState.initiator else tradeState.receiver
    val partnerTradeData = if(tradeState.initiator.userId == context.author.id) tradeState.receiver else tradeState.initiator

    val authorUserData = context.bot.database.userRepository.getUser(authorTradeData.userId)
    val partnerUserData = context.bot.database.userRepository.getUser(partnerTradeData.userId)

    val authorPokemon = context.bot.database.pokemonRepository.getPokemonByIds(authorTradeData.pokemon)
    val partnerPokemon = context.bot.database.pokemonRepository.getPokemonByIds(partnerTradeData.pokemon)

    val authorPokemonText = TradingModule.getTradeStatePokemonText(context, authorPokemon, partnerPokemon.map { it.id }, false)
    val partnerPokemonText = TradingModule.getTradeStatePokemonText(context, partnerPokemon, authorPokemon.map { it.id }, false)

    val statusTitle =
      if(authorTradeData.confirmed || partnerTradeData.confirmed)
        context.translate(
          "module.trading.commands.status.embeds.status.title",
          "confirmator" to if(authorTradeData.confirmed) authorUserData.tag.toString() else partnerUserData.tag.toString()
        )
      else null

    context.reply(
      context.embedTemplates
        .normal(
          context.translate(
            "modules.trading.commands.status.embeds.status.description",
            mapOf(
              "author" to context.author.asMention,
              "partner" to partnerUser.asMention
            )
          ),
          statusTitle
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
}