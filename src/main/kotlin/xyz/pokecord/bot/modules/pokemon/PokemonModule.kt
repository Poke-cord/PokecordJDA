package xyz.pokecord.bot.modules.pokemon

import net.dv8tion.jda.api.hooks.EventListener
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.pokemon.commands.*

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
    ReleaseCommand(),
    GiftCommand(),
    GiftCommand.GiftCreditCommand(),
    GiftCommand.GiftPokemonCommand(),
    MovesetCommand(),
    MoveCommand(),
    PokedexCommand(),
    OrderCommand()
  ),
//  arrayOf(
//    SpawnerEvent(),
//    XPGainEvent()
//  )
), EventListener {
  override val name = "Pokémon"
}
