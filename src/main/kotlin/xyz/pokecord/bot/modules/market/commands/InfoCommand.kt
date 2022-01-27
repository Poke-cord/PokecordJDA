package xyz.pokecord.bot.modules.market.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.core.structures.pokemon.Stat

object InfoCommand : Command() {
  override val name = "Info"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument listingId: Int?
  ) {
    if (!context.hasStarted(true)) return

    if (listingId == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.market.commands.info.errors.noListingId")
        ).build()
      ).queue()
      return
    }

    val listing = context.bot.database.marketRepository.getListing(listingId)
    if (listing == null || listing.sold || listing.unlisted) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.market.commands.info.errors.noListingFound", "id" to listingId.toString())
        ).build()
      ).queue()
    } else {
      val listingPokemon = context.bot.database.pokemonRepository.getPokemonById(listing.pokemon)
      if (listingPokemon != null) {
        val pokemon = Pokemon.getById(listingPokemon.id)

        val infoSection = """
        **${context.translate("misc.texts.listingId")}**: ${listing.id}
        **${context.translate("misc.texts.price")}**: ${context.translator.numberFormat(listing.price)}
        
        **${context.translate("misc.texts.xp")}**: ${if (listingPokemon.level >= 100) "Max" else "${listingPokemon.xp}/${listingPokemon.requiredXpToLevelUp()}"}
        **${context.translate("misc.texts.gender")}**: ${context.translator.gender(listingPokemon)}
        **${context.translate("misc.texts.nature")}**: ${context.translator.nature(listingPokemon.nature)}
        """.trimIndent()

        val statSection = """
        **${context.translator.stat(Stat.hp)}**: ${listingPokemon.stats.hp} - ${listingPokemon.ivs.hp}/31
        **${context.translator.stat(Stat.attack)}**: ${listingPokemon.stats.attack} - ${listingPokemon.ivs.attack}/31
        **${context.translator.stat(Stat.defense)}**: ${listingPokemon.stats.defense} - ${listingPokemon.ivs.defense}/31
        **${context.translator.stat(Stat.specialAttack)}**: ${listingPokemon.stats.specialAttack} - ${listingPokemon.ivs.specialAttack}/31
        **${context.translator.stat(Stat.specialDefense)}**: ${listingPokemon.stats.specialDefense} - ${listingPokemon.ivs.specialDefense}/31
        **${context.translator.stat(Stat.speed)}**: ${listingPokemon.stats.speed} - ${listingPokemon.ivs.speed}/31
        **${context.translate("misc.texts.totalIv")}**: ${listingPokemon.ivPercentage}
        """.trimIndent()

        context.reply(
          context.embedTemplates
            .normal(
              infoSection + "\n" + statSection,
              context.translate(
                "modules.market.commands.info.title",
                mapOf(
                  "pokemonLevel" to listingPokemon.level.toString(),
                  "pokemonName" to context.translator.pokemonDisplayName(listingPokemon, false),
                  "listingId" to listing.id.toString()
                )
              )
            )
            .setColor(pokemon!!.species.color.colorCode)
            .setImage(listingPokemon.imageUrl)
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