package xyz.pokecord.bot.modules.developer

import xyz.pokecord.bot.core.structures.discord.Command
import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext
import xyz.pokecord.bot.utils.Config

abstract class DeveloperCommand : Command() {
  override fun canRun(context: MessageReceivedContext): Boolean {
    return Config.devs.contains(context.author.id)
  }
}
