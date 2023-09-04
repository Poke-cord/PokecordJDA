package xyz.pokecord.bot.modules.nursery.commands

import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import java.io.File
import com.google.gson.Gson
import com.sun.tools.javac.code.TypeAnnotationPosition.field
import xyz.pokecord.bot.core.managers.database.repositories.DaycareRepository
import xyz.pokecord.bot.utils.PokemonResolvable

class DaycareCommand : Command() {
  override val name = "daycare"
  override var aliases = arrayOf("ns","nurse")
  @Executor
  suspend fun execute(

    context: ICommandContext,
    @Argument(name = "pokemon", consumeRest = true) pokemonNameOrId: PokemonResolvable?
   // @Argument   partnerName: String?,
  ) {

    if (pokemonNameOrId == null  ) {
      context.reply(
        context.embedTemplates.error("Put Pokemon name Dumbshit").build()
      ).queue()
      return
    }

    val id = pokemonNameOrId
    val pokemon = if (id != null) Pokemon.getById(id)
    else
      Pokemon.getByName(pokemonNameOrId.toString())
  //  val partner = Pokemon.getByName(partnerName)

    if (pokemon == null ) {
      context.reply(context.embedTemplates.error(context.translate("misc.errors.pokemonNotFound")).build())
        .queue()
      return
    }
    var userData = context.getUserData()
    val ownPokemon = context.resolvePokemon(context.author, userData, pokemonNameOrId)


    if (ownPokemon == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("misc.errors.pokemonNotFound")
        ).build()
      ).queue()
      return
    }




    // check ---

    val daycareRepo = DaycareRepository()


    val pokemons = daycareRepo.getPokemonsForUser(user)

    // Build embed with pokemon list
    val embed = context.embedTemplates.menu("Your Pokemons in Daycare") {


      for (pokemon in pokemons) {
        field {
          name = pokemon.name
          var value = "Level: ${pokemon.level}"
        }
      }
    }
    context.reply {
      embed(embed)
    }



    val pokemonNameSearch = pokemonName.toString()
      //val partnerNameSearch = partnerName.toString()
      val pokemonData = readJson("/data/pokemon.json")
      val pokemonObj = pokemonData.find { it.name == pokemonNameSearch }
    //  val partnerObj = pokemonData.find { it.name == partnerNameSearch }

// Get the speciesId
      val speciesId = pokemonObj?.speciesId
   //   val partnerSpcId = partnerObj?.speciesId

      if(speciesId != null  ) {
        // do something with speciesId
      }
      else
      {
        // Pokemon not found
      }}

    // val pokemonResolvable = PokemonResolvable(pokemon)
    // val partnerResolvable = PokemonResolvable(partner)
    // val resolvedPokemon = pokemonResolvable.resolve()
    //  val resolvedPartner = partnerResolvable.resolve()



    // if(!pokemon.canBreedWith(partner)) {
    //   respond {
    //    content = "These pokemon are not compatible for breeding!"
    //  }
    return
  }
  //val newPokemon = PokemonBreeder.breed(pokemon1, pokemon2)

  // Save new pokemon
  // pokemonStorage.savePokemon(newPokemon)

  // Send confirmation message
  //  respond {
  //    content = "You bred a new ${newPokemon.name}!"
  // }
}

fun readJson(path: String): List<Pokemon> {

  // Read the file
  val jsonString = File(path).readText()

  // Parse JSON into Pokemon list
  val gson = Gson()
  return gson.fromJson(jsonString, Array<Pokemon>::class.java).toList()

}


}
