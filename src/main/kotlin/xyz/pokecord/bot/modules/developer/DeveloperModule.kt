package xyz.pokecord.bot.modules.developer

import net.dv8tion.jda.api.hooks.EventListener
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.Module
import xyz.pokecord.bot.modules.developer.commands.EvalCommand
import xyz.pokecord.bot.modules.developer.commands.MockCommand
import xyz.pokecord.bot.modules.developer.commands.ShowCommand

class DeveloperModule(bot: Bot) : Module(
  bot,
  arrayOf(
    EvalCommand(),
    ShowCommand(),
    MockCommand()
  )
), EventListener {
  override val name = "Developer"
}
