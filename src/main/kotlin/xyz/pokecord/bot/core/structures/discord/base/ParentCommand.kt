package xyz.pokecord.bot.core.structures.discord.base

import xyz.pokecord.bot.api.ICommandContext

abstract class ParentCommand : Command() {
  abstract val childCommands: MutableList<Command>

  @Executor
  suspend fun execute(context: ICommandContext) {
    val embed = module.bot.getChildrenCommandListEmbed(context, this, childCommands) ?: return
    context.reply(embed.build()).queue()
  }
}
