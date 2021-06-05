package xyz.pokecord.bot.modules.developer

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.Config

abstract class DeveloperCommand : Command() {
  override fun canRun(context: ICommandContext): Boolean {
    return Config.devs.contains(context.author.id)
  }
}
