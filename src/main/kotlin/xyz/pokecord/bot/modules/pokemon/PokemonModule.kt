package xyz.pokecord.bot.modules.pokemon

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.pokemon.commands.*
import xyz.pokecord.bot.modules.pokemon.events.SpawnerEvent
import xyz.pokecord.bot.modules.pokemon.events.XPGainEvent

class PokemonModule(bot: Bot) : Module(
  bot,
  arrayOf(
    CatchCommand(),
    PickCommand(),
    PokemonCommand(),
    InfoCommand(),
    FavoriteCommand(),
    HintCommand(),
    NicknameCommand(),
    SelectCommand(),
    GiftCommand,
    MovesetCommand(),
    MoveCommand(),
    PokedexCommand(),
    OrderCommand(),
    breedPokemonCommand()
  ),
  arrayOf(
    SpawnerEvent(),
    XPGainEvent()
  )
) {
  override val name = "Pokémon"
}
