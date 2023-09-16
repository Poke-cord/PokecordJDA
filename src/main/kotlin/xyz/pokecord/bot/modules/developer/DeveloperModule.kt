package xyz.pokecord.bot.modules.developer

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.modules.developer.commands.*
import xyz.pokecord.bot.modules.developer.tasks.StatsSyncTask
import xyz.pokecord.bot.core.structures.discord.base.Module

class DeveloperModule(bot: Bot) : Module(
  bot,
  arrayOf(
    BigLeaderboardCommand(),
    EvalCommand(),
    GiveRedeemCommand,
    GiveRoleCommand,
    MockCommand(),
    ParseEntitiesCommand()
  ),
  tasks = arrayOf(
    StatsSyncTask()
  )
) {
  override val name = "Developer"
}
