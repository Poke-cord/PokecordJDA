package xyz.pokecord.bot.modules.developer

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.developer.commands.EvalCommand
import xyz.pokecord.bot.modules.developer.commands.MockCommand
import xyz.pokecord.bot.modules.developer.commands.ParseEntitiesCommand
import xyz.pokecord.bot.modules.developer.tasks.StatsSyncTask

class DeveloperModule(bot: Bot) : Module(
  bot,
  arrayOf(
    EvalCommand(),
    ParseEntitiesCommand(),
    MockCommand()
  ),
  tasks = arrayOf(
    StatsSyncTask()
  )
) {
  override val name = "Developer"
}
