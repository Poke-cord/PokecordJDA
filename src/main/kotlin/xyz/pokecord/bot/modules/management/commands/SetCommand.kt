package xyz.pokecord.bot.modules.management.commands

import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.modules.management.commands.set.SetLevelChannelCommand
import xyz.pokecord.bot.modules.management.commands.set.SetSilenceCommand
import xyz.pokecord.bot.modules.management.commands.set.SetSpawnChannelCommand

class SetCommand : ParentCommand() {
  override val childCommands =
    //mutableListOf(SetPrefixCommand, SetPrivateCommand, SetSilenceCommand, SetSpawnChannelCommand, SetLevelChannelCommand)
    mutableListOf(SetSilenceCommand, SetSpawnChannelCommand, SetLevelChannelCommand)
  override val name = "Set"

  override var aliases = arrayOf("settings", "setting", "config")
}
