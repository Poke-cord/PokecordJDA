package xyz.pokecord.bot.modules.general.commands

import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.modules.general.commands.startup.*

class StartupCommand : ParentCommand() {
  override val childCommands =
    mutableListOf(StartupIntroCommand)
  override val name = "Startup"

  override var aliases = arrayOf("starting", "greeting", "intro", "introduction")
}