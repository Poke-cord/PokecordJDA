package xyz.pokecord.bot.modules.nursery

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.pokemon.commands.*
import xyz.pokecord.bot.modules.pokemon.events.SpawnerEvent
import xyz.pokecord.bot.modules.pokemon.events.XPGainEvent

class NurseryModule (bot: Bot) : Module(
  bot,
  arrayOf(
    BreedCommand()
  ),
  arrayOf(
    SpawnerEvent(),
    XPGainEvent()
  )
)

{
    override val name = "Pokémon"
  }

