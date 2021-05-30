package xyz.pokecord.bot.core.structures.discord

import xyz.pokecord.bot.utils.EmbedPaginator

abstract class ParentCommand : Command() {
  val childCommands = mutableListOf<Command>()

  @Executor
  suspend fun execute(context: MessageReceivedContext) {
    val embeds = module.bot.getHelpEmbeds(context, childCommands)
    if (embeds.isNotEmpty()) {
      val paginator = EmbedPaginator(context, embeds.size, {
        embeds[it]
      }, 0)
      paginator.start()
    }
  }
}
