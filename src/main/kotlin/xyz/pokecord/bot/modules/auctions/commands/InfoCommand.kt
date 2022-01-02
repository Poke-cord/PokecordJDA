package xyz.pokecord.bot.modules.auctions.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.core.structures.pokemon.Stat
import xyz.pokecord.bot.utils.extensions.humanizeMs

object InfoCommand : Command() {
  override val name = "Info"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument auctionId: Int?
  ) {
    if (!context.hasStarted(true)) return

    if (auctionId == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.auctions.commands.info.errors.noAuctionId")
        ).build()
      ).queue()
      return
    }

    val auction = context.bot.database.auctionRepository.getAuction(auctionId)
    if (auction == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.auctions.commands.info.errors.noAuctionFound", "id" to auctionId.toString())
        ).build()
      ).queue()
    } else {
      val auctionPokemon = context.bot.database.pokemonRepository.getPokemonById(auction.pokemon)
      if (auctionPokemon != null) {
        val pokemon = Pokemon.getById(auctionPokemon.id)

        val infoSection = """
        **${context.translate("misc.texts.auctionId")}**: ${auction.id}
        **${context.translate("misc.texts.startingBid")}**: ${auction.startingBid}
        **${context.translate("misc.texts.bidCount")}**: ${auction.bids.size}
        **${context.translate("misc.texts.timeLeft")}**: ${auction.timeLeft.humanizeMs()}
        **${context.translate("misc.texts.currentBid")}**: ${auction.highestBid?.amount ?: context.translate("misc.texts.noBids")}
        
        **${context.translate("misc.texts.xp")}**: ${if (auctionPokemon.level >= 100) "Max" else "${auctionPokemon.xp}/${auctionPokemon.requiredXpToLevelUp()}"}
        **${context.translate("misc.texts.gender")}**: ${context.translator.gender(auctionPokemon)}
        **${context.translate("misc.texts.nature")}**: ${context.translator.nature(auctionPokemon.nature)}
        """.trimIndent()

        val statSection = """
        **${context.translator.stat(Stat.hp)}**: ${auctionPokemon.stats.hp} - ${auctionPokemon.ivs.hp}/31
        **${context.translator.stat(Stat.attack)}**: ${auctionPokemon.stats.attack} - ${auctionPokemon.ivs.attack}/31
        **${context.translator.stat(Stat.defense)}**: ${auctionPokemon.stats.defense} - ${auctionPokemon.ivs.defense}/31
        **${context.translator.stat(Stat.specialAttack)}**: ${auctionPokemon.stats.specialAttack} - ${auctionPokemon.ivs.specialAttack}/31
        **${context.translator.stat(Stat.specialDefense)}**: ${auctionPokemon.stats.specialDefense} - ${auctionPokemon.ivs.specialDefense}/31
        **${context.translator.stat(Stat.speed)}**: ${auctionPokemon.stats.speed} - ${auctionPokemon.ivs.speed}/31
        **${context.translate("misc.texts.totalIv")}**: ${auctionPokemon.ivPercentage}
        """.trimIndent()

        context.reply(
          context.embedTemplates
            .normal(
              infoSection + "\n" + statSection,
              context.translate(
                "modules.auctions.commands.info.title",
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