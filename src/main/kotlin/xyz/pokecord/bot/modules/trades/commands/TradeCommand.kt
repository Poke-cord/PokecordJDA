package xyz.pokecord.bot.modules.trades.commands

import net.dv8tion.jda.api.EmbedBuilder
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.EmbedTemplates
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.utils.EmbedPaginator

class TradeCommand: ParentCommand() {
  override val name = "trade"

  @Executor
  suspend fun execute(context: ICommandContext) {
    if (!context.hasStarted(true)) return
    context.reply("command testing").queue()
  }

  @ChildCommand
  class TradeStatusCommand: Command() {
    override val name = "status"

    @Executor
    suspend fun execute(context: ICommandContext) {
      if (!context.hasStarted(true)) return
      context.reply("Sub command testing").queue()
    }
  }
}