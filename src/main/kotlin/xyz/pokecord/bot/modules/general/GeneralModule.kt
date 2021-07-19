package xyz.pokecord.bot.modules.general

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.general.commands.*
import xyz.pokecord.bot.modules.general.events.ReadyEvent

class GeneralModule(bot: Bot) : Module(
  bot,
  arrayOf(
    HelpCommand(),
    PingCommand(),
    InviteCommand(),
    FAQCommand(),
    LeaderboardCommand(),
    VoteCommand(),
    SetCommand(),
    SetCommand.SetPrefixCommand(),
    SetCommand.SetPrivateCommand(),
    SetCommand.SetSpawnChannelCommand(),
    SetCommand.SilenceCommand()
  ),
  arrayOf(
    ReadyEvent()
  )
) {
  override val name = "General"
}
