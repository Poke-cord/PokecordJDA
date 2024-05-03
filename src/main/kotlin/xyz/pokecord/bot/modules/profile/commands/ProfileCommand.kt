package xyz.pokecord.bot.modules.profile.commands

import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.modules.profile.commands.profile.*

object ProfileCommand : ParentCommand() {
  override val name = "Profile"
  override val childCommands = mutableListOf(ProfilePokedexCommand, ProfileStatisticsCommand)
  override var aliases = arrayOf("pf", "pr", "stats")
}
