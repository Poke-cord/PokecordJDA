package xyz.pokecord.bot.modules.release.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.modules.trading.TradingModule

object ReleaseStatusCommand : Command() {
  override val name: String = "status"

  @Executor
  suspend fun execute(
    context: ICommandContext
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
    if(!releaseState.initiator.releaseTrade) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.pokemon.commands.release.errors.inTrade")
        ).build()
      ).queue()
      return
    }

    val authorReleaseData = releaseState.initiator

    val authorPokemon = context.bot.database.pokemonRepository.getPokemonByIds(authorReleaseData.pokemon)

    val authorPokemonText = TradingModule.getTradeStatePokemonText(context, authorPokemon, authorPokemon.map { it.id }, false)


    context.reply(
      context.embedTemplates.normal(
        authorPokemonText.joinToString("\n").ifEmpty { "None" },
        "Release status"
      ).build()
    ).queue()
    return
  }
}