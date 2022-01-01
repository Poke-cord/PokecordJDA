package xyz.pokecord.bot.modules.general.commands

import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.modules.general.commands.set.SetPrefixCommand
import xyz.pokecord.bot.modules.general.commands.set.SetPrivateCommand
import xyz.pokecord.bot.modules.general.commands.set.SetSilenceCommand
import xyz.pokecord.bot.modules.general.commands.set.SetSpawnChannelCommand

class SetCommand : ParentCommand() {
  override val childCommands =
    mutableListOf(SetPrefixCommand, SetPrivateCommand, SetSilenceCommand, SetSpawnChannelCommand)
  override val name = "Set"

  override var aliases = arrayOf("settings", "setting", "config")
}
