package xyz.pokecord.bot.modules.pokemon.commands

import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.modules.pokemon.gift.GiftCreditCommand
import xyz.pokecord.bot.modules.pokemon.gift.GiftPokemonCommand
import xyz.pokecord.bot.modules.pokemon.gift.GiftToggleCommand

class GiftCommand : ParentCommand() {
  override val childCommands = mutableListOf(GiftCreditCommand, GiftPokemonCommand, GiftToggleCommand)
  override val name = "Gift"

  override var excludeFromHelp = true
}
