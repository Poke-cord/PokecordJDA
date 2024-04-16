package xyz.pokecord.bot.modules.profile

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.profile.commands.*


class ProfileModule(bot: Bot): Module(
  bot,
  arrayOf(
    BagCommand(),
    PrivateCommand,
    ProfileCommand(),
  )
) {
  override val name = "Profile"
}