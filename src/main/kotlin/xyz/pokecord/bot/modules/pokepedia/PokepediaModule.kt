package xyz.pokecord.bot.modules.pokepedia

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.pokepedia.commands.*

class PokepediaModule(bot: Bot): Module(
  bot,
  arrayOf(
    ForecastCommand,
    MoveCommand(),
    MovesetCommand(),
    PokedexCommand(),
  )
) {
  override val name = "Poképedia"
}