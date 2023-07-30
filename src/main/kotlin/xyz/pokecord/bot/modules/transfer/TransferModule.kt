package xyz.pokecord.bot.modules.transfer

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.transfer.commands.TransferCommand

class TransferModule(bot: Bot) : Module(
  bot,
  arrayOf(TransferCommand)
) {
  override val name: String = "Transfer"

  companion object {
    suspend fun getTransferStatePokemonText(
      context: ICommandContext,
      pokemon: List<OwnedPokemon>
    ): List<String> {
      return pokemon.map {
        val name = context.translator.pokemonDisplayName(it, false)

        "|`${it.index + 1}`| **$name** │ **${it.ivPercentage}** IV @ LVL **${it.level}**"
      }
    }
  }
}