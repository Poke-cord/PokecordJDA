package xyz.pokecord.bot.core.structures.discord.base

abstract class ParentCommand : Command() {
  val childCommands = mutableListOf<Command>()

  @Executor
  suspend fun execute(context: BaseCommandContext) {
    val embed = module.bot.getChildrenCommandListEmbed(context, this, childCommands) ?: return
    context.reply(embed.build()).queue()
  }
}
