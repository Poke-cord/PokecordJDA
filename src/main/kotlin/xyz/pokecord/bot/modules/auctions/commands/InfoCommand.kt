package xyz.pokecord.bot.modules.auctions.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
// import xyz.pokecord.bot.core.structures.pokemon.Stat
import xyz.pokecord.bot.utils.extensions.humanizeMs

object InfoCommand : Command() {
  override val name = "Info"
  override var aliases = arrayOf("i")

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
    if (auction == null || auction.ended) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.auctions.commands.info.errors.noAuctionFound", "id" to auctionId.toString())
        ).build()
      ).queue()
    } else {
      val auctionPokemon = context.bot.database.pokemonRepository.getPokemonById(auction.pokemon)
      if (auctionPokemon != null) {
        val pokemon = Pokemon.getById(auctionPokemon.id)

        val auInfoSection = """
        **${context.translate("misc.texts.auctionId")}**: ${auction.id}
        **${context.translate("misc.texts.bidCount")}**: ${if (auction.bids.size > 0) auction.bids.size else context.translate("misc.texts.noBids")}
        **${if (auction.bids.size > 0) context.translate("misc.texts.currentBid") else context.translate("misc.texts.startingBid")}**: ${
          auction.highestBid?.amount?.let {
            context.translator.numberFormat(
              it
            )
          } ?: context.translator.numberFormat(auction.startingBid)
        }
        **${context.translate("misc.texts.timeLeft")}**: ${auction.timeLeft.humanizeMs()}
        """.trimIndent()

//     **${context.translate("misc.texts.startingCurrentBid")}**
//        > ${context.translator.numberFormat(auction.startingBid)} / ${'$'}{
//          auction.highestBid?.amount?.let {
//            context.translator.numberFormat(
//              it
//          } ?: context.translate("misc.texts.noBids")
//        }

        val infoSection = """
        **${context.translate("misc.texts.xp")}**: ${if (auctionPokemon.level >= 100) "Max" else "${auctionPokemon.xp}/${auctionPokemon.requiredXpToLevelUp()}"}
        **${context.translate("misc.texts.gender")}**: ${context.translator.gender(auctionPokemon)}
        **${context.translate("misc.texts.nature")}**: ${context.translator.nature(auctionPokemon.nature)}
        """.trimIndent()

        val statSection = """
        **Stat Spread (IV, +EV)**
        `${context.translate("misc.texts.hp").padEnd(7)}| ${auctionPokemon.stats.hp.toString().padStart(3)} | ${auctionPokemon.ivs.hp.toString().padStart(2)}/31${ if(auctionPokemon.evs.hp > 0) ("+" + (auctionPokemon.evs.hp).toString()).padStart(6) else " ".repeat(6)}`
        `${context.translate("misc.texts.attack").padEnd(7)}| ${auctionPokemon.stats.attack.toString().padStart(3)} | ${auctionPokemon.ivs.attack.toString().padStart(2)}/31${ if(auctionPokemon.evs.attack > 0) ("+" + (auctionPokemon.evs.attack).toString()).padStart(6) else " ".repeat(6)}`
        `${context.translate("misc.texts.defense").padEnd(7)}| ${auctionPokemon.stats.defense.toString().padStart(3)} | ${auctionPokemon.ivs.defense.toString().padStart(2)}/31${ if(auctionPokemon.evs.defense > 0) ("+" + (auctionPokemon.evs.defense).toString()).padStart(6) else " ".repeat(6)}`
        `${context.translate("misc.texts.specialAttack").padEnd(7)}| ${auctionPokemon.stats.specialAttack.toString().padStart(3)} | ${auctionPokemon.ivs.specialAttack.toString().padStart(2)}/31${ if(auctionPokemon.evs.specialAttack > 0) ("+" + (auctionPokemon.evs.specialAttack).toString()).padStart(6) else " ".repeat(6)}`
        `${context.translate("misc.texts.specialDefense").padEnd(7)}| ${auctionPokemon.stats.specialDefense.toString().padStart(3)} | ${auctionPokemon.ivs.specialDefense.toString().padStart(2)}/31${ if(auctionPokemon.evs.specialDefense > 0) ("+" + (auctionPokemon.evs.specialDefense).toString()).padStart(6) else " ".repeat(6)}`
        `${context.translate("misc.texts.speed").padEnd(7)}| ${auctionPokemon.stats.speed.toString().padStart(3)} | ${auctionPokemon.ivs.speed.toString().padStart(2)}/31${ if(auctionPokemon.evs.speed > 0) ("+" + (auctionPokemon.evs.speed).toString()).padStart(6) else " ".repeat(6)}`
        **${context.translate("misc.texts.totalIv")}**: ${auctionPokemon.ivPercentage}
        """.trimIndent()

        context.reply(
          context.embedTemplates
            .normal(
              auInfoSection + "\n\n" + infoSection + "\n\n" + statSection,
              context.translate(
                "modules.auctions.commands.info.title",
                mapOf(
                  "pokemonLevel" to auctionPokemon.level.toString(),
                  "pokemonName" to context.translator.pokemonDisplayName(auctionPokemon, false),
                  "auctionId" to auction.id.toString()
                )
              )
            )
            .setColor(pokemon!!.species.color.colorCode)
            .setImage(auctionPokemon.imageUrl)
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