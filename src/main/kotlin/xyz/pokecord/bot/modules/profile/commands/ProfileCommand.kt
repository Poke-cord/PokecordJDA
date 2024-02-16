package xyz.pokecord.bot.modules.profile.commands

import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.modules.profile.commands.profile.ProfileChecklistCommand
import xyz.pokecord.bot.modules.profile.commands.profile.ProfileInfoCommand
import xyz.pokecord.bot.modules.profile.commands.profile.ProfilePokedexCommand

class ProfileCommand : ParentCommand() {
  override val childCommands = mutableListOf<Command>(ProfileChecklistCommand,ProfileInfoCommand,ProfilePokedexCommand)
  override val name = "Profile"
}
