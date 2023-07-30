package xyz.pokecord.bot.modules.transfer.commands

import xyz.pokecord.bot.core.structures.discord.base.ParentCommand

object TransferCommand : ParentCommand() {
  override val childCommands =
    mutableListOf(
      TransferStartCommand, TransferAddCommand, TransferRemoveCommand,
      TransferCancelCommand, TransferConfirmCommand, TransferStatusCommand
    )
  override val name = "Transfer"

  override var aliases = arrayOf("ts", "tr", "release", "r", "rl")
}
