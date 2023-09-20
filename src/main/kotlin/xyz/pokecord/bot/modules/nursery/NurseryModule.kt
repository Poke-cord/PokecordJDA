package xyz.pokecord.bot.modules.nursery

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.nursery.commands.*

class NurseryModule (bot: Bot) : Module(
  bot,
  arrayOf(
    DaycareCommand(),
    BreedCommands()
  )
)

{
  override val name = "Nursery"
}

