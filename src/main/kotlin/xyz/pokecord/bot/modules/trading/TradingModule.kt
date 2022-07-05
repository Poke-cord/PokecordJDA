package xyz.pokecord.bot.modules.trading

import com.mongodb.reactivestreams.client.ClientSession
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.trading.commands.TradeCommand

class TradingModule(bot: Bot) : Module(
  bot,
  arrayOf(TradeCommand)
) {
  override val name = "Trading"

  companion object {
    suspend fun getTradeStatePokemonText(
      context: ICommandContext,
      pokemon: List<OwnedPokemon>,
      partnerPokemonIds: List<Int>,
      updateInDb: Boolean,
      clientSession: ClientSession? = null
    ): List<String> {
      return pokemon.map {
        val initialName = context.translator.pokemonDisplayName(it, false)
        val (_, evolved) = context.bot.database.pokemonRepository.levelUpAndEvolveIfPossible(
          it, null, null, partnerPokemonIds, updateInDb, clientSession
        )

        val evolutionNameText = if (evolved) "-> ${context.translator.pokemonName(it)}" else ""
        "${it.index + 1} | ${initialName}${evolutionNameText} - ${it.level} - ${it.ivPercentage}"
      }
    }
  }
}