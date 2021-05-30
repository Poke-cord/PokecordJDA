package xyz.pokecord.bot.modules.general

import net.dv8tion.jda.api.hooks.EventListener
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.Module
import xyz.pokecord.bot.modules.general.commands.*
import xyz.pokecord.bot.modules.general.events.ReadyEvent
import xyz.pokecord.bot.modules.general.jobs.PingLoggerJob

class GeneralModule(bot: Bot) : Module(
  bot,
  arrayOf(
    HelpCommand(),
    PingCommand(),
    InviteCommand(),
    FAQCommand(),
    ParseEntitiesCommand(),
    LeaderboardCommand(),
    VoteCommand(),
    SetCommand(),
    SetCommand.SetPrefixCommand(),
    SetCommand.SetPrivateCommand(),
    SetCommand.SetSpawnChannelCommand()
  ),
  arrayOf(
    ReadyEvent()
  ),
  arrayOf(
    PingLoggerJob()
  )
), EventListener {
  override val name = "General"
}
