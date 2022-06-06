package xyz.pokecord.bot.modules.release

import com.mongodb.reactivestreams.client.ClientSession
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.release.commands.ReleaseCommand

class ReleaseModule(bot: Bot) : Module (
  bot,
  arrayOf(ReleaseCommand)
) {
  override val name: String = "Release"

  companion object {
  }
}