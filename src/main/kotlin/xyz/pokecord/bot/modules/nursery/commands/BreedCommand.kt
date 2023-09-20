package xyz.pokecord.bot.modules.nursery.commands

import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.utils.PokemonResolvable
import xyz.pokecord.bot.modules.nursery.NurseryModule.*
import java.io.File
import com.google.gson.Gson

class BreedCommand : Command() {
  override val name = "breed"
  @Executor
  suspend fun execute(

    context: ICommandContext,
    @Argument   pokemonName: String?,
    @Argument   partnerName: String?,
  ) {

    if (pokemonName == null || partnerName == null) {
      context.reply(
        context.embedTemplates.error(context.translate("modules.pokemon.commands.pick.noNameProvided")).build()
      ).queue()
      return
    }

    val pokemon = Pokemon.getByName(pokemonName)
    val partner = Pokemon.getByName(partnerName)

    if (pokemon == null || partner == null) {
      context.reply(context.embedTemplates.error(context.translate("misc.errors.pokemonNotFound")).build())
        .queue()
      return
    }
 else {
      val pokemonNameSearch = pokemonName.toString()
      val partnerNameSearch = partnerName.toString()
      val pokemonData = readJson("/data/pokemon.json")
      val pokemonObj = pokemonData.find { it.name == pokemonNameSearch }
      val partnerObj = pokemonData.find { it.name == partnerNameSearch }

// Get the speciesId
      val speciesId = pokemonObj?.speciesId
      val partnerSpcId = partnerObj?.speciesId

      if(speciesId != null && partnerSpcId != null) {
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
  fun canBreedWith(other: Pokemon): Boolean {

    // Check if in the same egg group
  //  if(this.eggGroup != other.eggGroup) {
   //   return false
   // }

    // Check if opposite genders
   // if(this.gender == other.gender) {
   //   return false
   // }
    return true
  //}

  }
