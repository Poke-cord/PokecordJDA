package xyz.pokecord.bot.modules.auctions.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object ListCommand: Command() {
  override val name = "List"
  override var aliases = arrayOf("create")

  @Executor
  suspend fun execute(context: ICommandContext) {

  }
}