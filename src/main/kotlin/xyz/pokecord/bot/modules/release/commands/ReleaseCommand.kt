package xyz.pokecord.bot.modules.release.commands

import xyz.pokecord.bot.core.structures.discord.base.ParentCommand

object ReleaseCommand : ParentCommand() {
  override val childCommands =
    mutableListOf(
      ReleaseStartCommand, ReleaseAddCommand, ReleaseRemoveCommand,
      ReleaseCancelCommand, ReleaseConfirmCommand, ReleaseStatusCommand
    )
  override val name = "Release"

  override var aliases = arrayOf("r", "rl", "transfer", "ts")
}
