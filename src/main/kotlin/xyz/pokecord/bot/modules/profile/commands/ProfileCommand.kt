package xyz.pokecord.bot.modules.profile.commands

import net.dv8tion.jda.api.EmbedBuilder
import xyz.pokecord.bot.core.structures.discord.Command
import xyz.pokecord.bot.core.structures.discord.EmbedTemplates
import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext
import xyz.pokecord.bot.core.structures.discord.ParentCommand
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.utils.EmbedPaginator

class ProfileCommand : ParentCommand() {
  override val name = "Profile"

  override var excludeFromHelp = true

  @ChildCommand
  class ProfilePokedexCommand : Command() {
    override val name = "Pokedex"

    override var aliases = arrayOf("dex", "d")

    @Executor
    suspend fun execute(
      context: MessageReceivedContext
    ) {
      if (!context.hasStarted(true)) return

      val userData = context.getUserData()

      val caughtSet = (userData.caughtPokemon + userData.caughtShinies).toSet().toList().sorted()
      EmbedPaginator(context, caughtSet.size / 15, { pageIndex ->
        val entries = mutableListOf<String>()
        val pokemonList = (pageIndex * 15 until (pageIndex + 1) * 15).mapNotNull { Pokemon.getById(caughtSet[it]) }
        val pokemonNames = pokemonList.mapNotNull { context.translator.pokemonName(it) }
        val longestPokemonNameLength = pokemonNames.maxOf { it.length }
        for (i in pokemonList.indices) {
          entries.add(
            "`${pokemonList[i].formattedSpeciesId} ${pokemonNames[i].padEnd(longestPokemonNameLength, ' ')}` - ${
              if (userData.caughtShinies.contains(
                  pokemonList[i].id
                )
              ) "⭐" else " ✅"
            }"
          )
        }
        EmbedBuilder()
          .setTitle("Caught Pokémon")
          .setColor(EmbedTemplates.Color.GREEN.code)
          .setDescription(entries.joinToString("\n"))
      }).start()
    }
  }
}
