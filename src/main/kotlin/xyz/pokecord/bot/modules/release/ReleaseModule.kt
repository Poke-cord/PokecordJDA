package xyz.pokecord.bot.modules.release

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.release.commands.ReleaseCommand

class ReleaseModule(bot: Bot) : Module(
  bot,
  arrayOf(ReleaseCommand)
) {
  override val name: String = "Release"

  companion object {
    suspend fun getReleaseStatePokemonText(
      context: ICommandContext,
      pokemon: List<OwnedPokemon>
    ): List<String> {
      return pokemon.map {
        val name = context.translator.pokemonDisplayName(it, false)

        "${it.index + 1} | $name - ${it.level} - ${it.ivPercentage}"
      }
    }
  }
}