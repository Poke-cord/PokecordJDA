package xyz.pokecord.bot.modules.profile

import net.dv8tion.jda.api.hooks.EventListener
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.Module
import xyz.pokecord.bot.modules.profile.commands.*

class ProfileModule(bot: Bot) : Module(
  bot,
  arrayOf(
    StartCommand(),
    BalanceCommand(),
    BagCommand(),
    ItemCommand(),
    ItemCommand.GiveItemCommand(),
    ItemCommand.TakeItemCommand(),
    ProfileCommand(),
    ProfileCommand.ProfilePokedexCommand(),
    RewardsCommand()
  )
), EventListener {
  override val name = "Profile"
}
