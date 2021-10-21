package xyz.pokecord.bot.modules.auctions.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.Pokemon

object InfoCommand: Command() {
  override val name = "Info"

  suspend fun execute(
    context: ICommandContext,
    @Argument auctionId: Int?
  ) {
    if(!context.hasStarted(true)) return

    if(auctionId == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.auctions.commands.info.errors.noAuctionId")
        ).build()
      ).queue()
      return
    }

    val auction = context.bot.database.auctionRepository.getAuction(auctionId)
    if(auction == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.auctions.commands.info.errors.noAuctionFound", "id" to auctionId.toString())
        ).build()
      ).queue()
    } else {
      val auctionPokemon = context.bot.database.pokemonRepository.getPokemonById(auction.pokemon)
      if(auctionPokemon != null) {
        val pokemon = Pokemon.getById(auctionPokemon.id)
        val highestBid = auction.highestBid
        val xp = if(auctionPokemon.level >= 100) "Max" else auctionPokemon.xp.toString() + "/" + auctionPokemon.requiredXpToLevelUp().toString() + "xp"

        context.reply(
          context.embedTemplates
            .normal(
              context.translate(
                "modules.auctions.commands.info.displayInfo.description",
                mapOf(
                  "auctionId" to auction.id.toString(),
                  "startingBid" to auction.startingBid.toString(),
                  "bidCount" to auction.bids.size.toString(),
                  "endingTime" to auction.endsAtTimestamp.toString(),
                  "currentBid" to (highestBid?.amount?.toString() ?: context.translate("misc.texts.noBids")),
                  "XP" to xp,
                  "gender" to context.translator.gender(auctionPokemon.gender),
                  "nature" to context.translator.nature(auctionPokemon.nature)!!,
                  "hp" to (auctionPokemon.stats.hp - auctionPokemon.ivs.hp).toString(),
                  "attack" to (auctionPokemon.stats.attack - auctionPokemon.ivs.attack).toString(),
                  "defence" to (auctionPokemon.stats.defense - auctionPokemon.ivs.defense).toString(),
                  "special-attack" to (auctionPokemon.stats.specialAttack - auctionPokemon.ivs.specialAttack).toString(),
                  "special-defense" to (auctionPokemon.stats.specialDefense - auctionPokemon.ivs.specialDefense).toString(),
                  "speed" to (auctionPokemon.stats.speed - auctionPokemon.ivs.speed).toString(),
                  "ivPercentage" to auctionPokemon.ivPercentage
                )
              ),
              context.translate(
                "modules.auctions.commands.info.displayInfo.title",
                mapOf(
                  "pokemonLevel" to auctionPokemon.level.toString(),
                  "pokemonName" to context.translator.pokemonDisplayName(auctionPokemon),
                  "auctionId" to auction.id.toString()
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
            context.translate("modules.auctions.commands.info.errors.noPokemonFound")
          ).build()
        ).queue()
      }
    }
  }
}