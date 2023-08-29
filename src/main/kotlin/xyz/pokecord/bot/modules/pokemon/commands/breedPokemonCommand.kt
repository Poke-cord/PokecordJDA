package xyz.pokecord.bot.modules.pokemon.commands

import xyz.pokecord.bot.core.structures.discord.base.Command

class breedPokemonCommand : Command() {
  override val name = "breed"

  override var rateLimit = 5000L
}