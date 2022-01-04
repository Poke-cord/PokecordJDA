package xyz.pokecord.bot.modules.developer

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.developer.commands.*
import xyz.pokecord.bot.modules.developer.tasks.RedisSyncTask

class DeveloperModule(bot: Bot) : Module(
  bot,
  arrayOf(
    EvalCommand(),
    ShowCommand(),
    ParseEntitiesCommand(),
    MockCommand()
  ),
  tasks = arrayOf(
    RedisSyncTask()
  )
) {
  override val name = "Developer"
}
