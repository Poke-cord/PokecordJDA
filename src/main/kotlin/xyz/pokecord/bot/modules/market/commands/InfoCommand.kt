package xyz.pokecord.bot.modules.market.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.core.structures.pokemon.Stat

object InfoCommand : Command() {
  override val name = "Info"
  override var aliases = arrayOf("i")
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

        val mkInfoSection = """
        **${context.translate("misc.texts.listingId")}**: ${listing.id}
        **${context.translate("misc.texts.price")}**: ${context.translator.numberFormat(listing.price)}
        """.trimIndent()

        val infoSection = """
        **${context.translate("misc.texts.xp")}**: ${if (listingPokemon.level >= 100) "Max" else "${listingPokemon.xp}/${listingPokemon.requiredXpToLevelUp()}"}
        **${context.translate("misc.texts.gender")}**: ${context.translator.gender(listingPokemon)}
        **${context.translate("misc.texts.nature")}**: ${context.translator.nature(listingPokemon.nature)}
        """.trimIndent()

        val statSection = """
        **Stat Spread (IV, +EV)**
        `${context.translate("misc.texts.hp").padEnd(7)}| ${listingPokemon.stats.hp.toString().padStart(3)} | ${listingPokemon.ivs.hp.toString().padStart(2)}/31${ if(listingPokemon.evs.hp > 0) ("+" + (listingPokemon.evs.hp).toString()).padStart(6) else " ".repeat(6)}`
        `${context.translate("misc.texts.attack").padEnd(7)}| ${listingPokemon.stats.attack.toString().padStart(3)} | ${listingPokemon.ivs.attack.toString().padStart(2)}/31${ if(listingPokemon.evs.attack > 0) ("+" + (listingPokemon.evs.attack).toString()).padStart(6) else " ".repeat(6)}`
        `${context.translate("misc.texts.defense").padEnd(7)}| ${listingPokemon.stats.defense.toString().padStart(3)} | ${listingPokemon.ivs.defense.toString().padStart(2)}/31${ if(listingPokemon.evs.defense > 0) ("+" + (listingPokemon.evs.defense).toString()).padStart(6) else " ".repeat(6)}`
        `${context.translate("misc.texts.specialAttack").padEnd(7)}| ${listingPokemon.stats.specialAttack.toString().padStart(3)} | ${listingPokemon.ivs.specialAttack.toString().padStart(2)}/31${ if(listingPokemon.evs.specialAttack > 0) ("+" + (listingPokemon.evs.specialAttack).toString()).padStart(6) else " ".repeat(6)}`
        `${context.translate("misc.texts.specialDefense").padEnd(7)}| ${listingPokemon.stats.specialDefense.toString().padStart(3)} | ${listingPokemon.ivs.specialDefense.toString().padStart(2)}/31${ if(listingPokemon.evs.specialDefense > 0) ("+" + (listingPokemon.evs.specialDefense).toString()).padStart(6) else " ".repeat(6)}`
        `${context.translate("misc.texts.speed").padEnd(7)}| ${listingPokemon.stats.speed.toString().padStart(3)} | ${listingPokemon.ivs.speed.toString().padStart(2)}/31${ if(listingPokemon.evs.speed > 0) ("+" + (listingPokemon.evs.speed).toString()).padStart(6) else " ".repeat(6)}`
        **${context.translate("misc.texts.totalIv")}**: ${listingPokemon.ivPercentage}
        """.trimIndent()

        context.reply(
          context.embedTemplates
            .normal(
              mkInfoSection + "\n\n" + infoSection + "\n\n" + statSection,
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