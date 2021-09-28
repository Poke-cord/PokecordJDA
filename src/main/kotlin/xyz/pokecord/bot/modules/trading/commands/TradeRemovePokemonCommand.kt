package xyz.pokecord.bot.modules.trading.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.PokemonResolvable

object TradeRemovePokemonCommand : Command() {
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
      return
    }

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
            "modules.trading.commands.remove.errors.noPokemonFound",
            "index" to pokemon.toString()
          )
        ).build()
      ).queue()
    } else {
      val transfer = selectedPokemon.transferable(context.bot.database)
      if (transfer != OwnedPokemon.TransferStates.TRADE_SESSION) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.trading.commands.remove.errors.notTransferable")
          ).build()
        ).queue()
        return
      }

      context.bot.database.tradeRepository.removePokemon(tradeState, context.author.id, selectedPokemon._id)

      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.trading.commands.remove.embeds.removePokemon.description",
            "pokemon" to selectedPokemon.displayName
          ),
          context.translate("modules.trading.commands.remove.embeds.removePokemon.title")
        ).build()
      ).queue()
    }
  }
}