package xyz.pokecord.bot.modules.general.commands

import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.modules.general.commands.set.*

class SetCommand : ParentCommand() {
  override val childCommands =
    //mutableListOf(SetPrefixCommand, SetPrivateCommand, SetSilenceCommand, SetSpawnChannelCommand, SetLevelChannelCommand)
    mutableListOf(SetPrivateCommand, SetSilenceCommand, SetSpawnChannelCommand, SetLevelChannelCommand)
  override val name = "Set"

  override var aliases = arrayOf("settings", "setting", "config")
}
