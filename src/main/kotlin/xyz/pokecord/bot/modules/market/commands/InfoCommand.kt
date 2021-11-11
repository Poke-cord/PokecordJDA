package xyz.pokecord.bot.modules.market.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.Pokemon

object InfoCommand: Command() {
  override val name = "Info"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument listingId: Int?
  ) {
    if(!context.hasStarted(true)) return

    if(listingId == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.market.commands.info.errors.noListingId")
        ).build()
      ).queue()
      return
    }

    val listing = context.bot.database.marketRepository.getListing(listingId)
    if(listing == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.market.commands.info.errors.noListingFound", "id" to listingId.toString())
        ).build()
      ).queue()
    } else {
      val listingPokemon = context.bot.database.pokemonRepository.getPokemonById(listing.pokemon)
      if(listingPokemon != null) {
        val pokemon = Pokemon.getById(listingPokemon.id)
        val xp = if(listingPokemon.level >= 100) "Max" else listingPokemon.xp.toString() + "/" + listingPokemon.requiredXpToLevelUp().toString() + "xp"

        context.reply(
          context.embedTemplates
            .normal(
              context.translate(
                "modules.market.commands.info.displayInfo.description",
                mapOf(
                  "listingId" to listing.id.toString(),
                  "price" to listing.price.toString(),
                  "XP" to xp,
                  "gender" to context.translator.gender(listingPokemon.gender),
                  "nature" to context.translator.nature(listingPokemon.nature)!!,
                  "hp" to (listingPokemon.stats.hp - listingPokemon.ivs.hp).toString(),
                  "attack" to (listingPokemon.stats.attack - listingPokemon.ivs.attack).toString(),
                  "defence" to (listingPokemon.stats.defense - listingPokemon.ivs.defense).toString(),
                  "special-attack" to (listingPokemon.stats.specialAttack - listingPokemon.ivs.specialAttack).toString(),
                  "special-defense" to (listingPokemon.stats.specialDefense - listingPokemon.ivs.specialDefense).toString(),
                  "speed" to (listingPokemon.stats.speed - listingPokemon.ivs.speed).toString(),
                  "ivPercentage" to listingPokemon.ivPercentage
                )
              ),
              context.translate(
                "modules.market.commands.info.displayInfo.title",
                mapOf(
                  "pokemonLevel" to listingPokemon.level.toString(),
                  "pokemonName" to context.translator.pokemonDisplayName(listingPokemon),
                  "listingId" to listing.id.toString()
                )
              )
            )
            .setColor(pokemon!!.species.color.colorCode)
            .setImage(pokemon.imageUrl)
            .build()
        ).queue()
      } else {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.market.commands.info.errors.noPokemonFound")
          ).build()
        ).queue()
      }
    }
  }
}