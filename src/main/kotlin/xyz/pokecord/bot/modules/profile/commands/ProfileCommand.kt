package xyz.pokecord.bot.modules.profile.commands

import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.modules.profile.commands.profile.ProfilePokedexCommand

object ProfileCommand : ParentCommand() {
  override val childCommands = mutableListOf<Command>(ProfilePokedexCommand)
  override val name = "Profile"
}
