package xyz.pokecord.bot.modules.nursery.commands

import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.modules.nursery.commands.PokemonBreeder
import xyz.pokecord.bot.api.ICommandContext

class BreedCommand : Command() {
  override val name = "breed"
  suspend fun execute(

    context: ICommandContext,
    @Argument  (name = "pokemon") pokemonName: String?,
    @Argument  (name = "pokemon") pokemonName2: String?
  ) {
    if (pokemonName == null) {
      context.reply(context.embedTemplates.error(context.translate("misc.errors.pokemonNotFound")).build())
        .queue()
      return
    }
    else if (pokemonName2 == null) {
      context.reply(context.embedTemplates.error(context.translate("misc.errors.pokemonNotFound")).build())
        .queue()
      return
    }

    val pokemon1Id = args[0]
    val pokemon2Id = args[1]

    // Lookup pokemon
    val pokemon1 = pokemonStorage.getPokemon(pokemon1Id)
    val pokemon2 = pokemonStorage.getPokemon(pokemon2Id)

    if(!pokemon1.canBreedWith(pokemon2)) {
      respond {
        content = "These pokemon are not compatible for breeding!"
      }
      return
    }
    val newPokemon = PokemonBreeder.breed(pokemon1, pokemon2)

    // Save new pokemon
    pokemonStorage.savePokemon(newPokemon)

    // Send confirmation message
    respond {
      content = "You bred a new ${newPokemon.name}!"
    }
  }
}
