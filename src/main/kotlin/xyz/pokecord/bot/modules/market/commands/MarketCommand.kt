package xyz.pokecord.bot.modules.market.commands

import net.dv8tion.jda.api.EmbedBuilder
import org.litote.kmongo.eq
import org.litote.kmongo.match
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.Listing
import xyz.pokecord.bot.core.structures.discord.EmbedTemplates
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.utils.EmbedPaginator
import kotlin.math.ceil

object MarketCommand : ParentCommand() {
  override val name = "Market"
  override var aliases = arrayOf("markets")
  override val childCommands: MutableList<Command> =
    mutableListOf(ListCommand, UnlistCommand, BuyCommand, InfoCommand, ProfileCommand)

  suspend fun formatListings(
    context: ICommandContext,
    listings: List<Listing>
  ): String {
    val desc = listings.map {
      val listingPokemon = context.bot.database.pokemonRepository.getPokemonById(it.pokemon)
      if (listingPokemon != null) {
        val pokemonIv = listingPokemon.ivPercentage
        val pokemonName = context.translator.pokemonDisplayName(listingPokemon, false)
        if (!it.sold) {
          "`${it.id}` IV **$pokemonIv $pokemonName** | **${it.price}** Credits"
        } else null
      } else null
    }

    return desc.filterNotNull().joinToString("\n")
  }

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(optional = true) page: Int?,
  ) {
    if (!context.hasStarted(true)) return

    val templateEmbedBuilder =
      EmbedBuilder()
        .setTitle(
          context.translate(
            "modules.market.commands.market.embeds.title"
          )
        )
        .setColor(EmbedTemplates.Color.GREEN.code)

    val aggregation = listOf(match(Listing::sold eq false, Listing::unlisted eq false))
    val count = context.bot.database.marketRepository.getListingCount(aggregation = aggregation.toMutableList())
    if (count < 1) {
      context.reply(
        templateEmbedBuilder.setDescription(context.translate("modules.market.commands.market.errors.noResults"))
          .setColor(EmbedTemplates.Color.RED.code).build()
      ).queue()
      return
    }

    val pageCount = ceil((count.toDouble() / 10)).toInt()
    val paginator = EmbedPaginator(context, pageCount, { pageIndex ->
      if (pageIndex >= pageCount) {
        return@EmbedPaginator templateEmbedBuilder.setDescription(context.translate("modules.market.commands.market.errors.noResults"))
          .setColor(EmbedTemplates.Color.RED.code).setFooter("")
      }
      val listings = context.bot.database.marketRepository.getListings(skip = pageIndex * 10, aggregation = aggregation.toMutableList())
      templateEmbedBuilder.clearFields().setFooter(null)
      templateEmbedBuilder.setDescription(formatListings(context, listings))
      templateEmbedBuilder
    }, if (page == null) 0 else page - 1)
    paginator.start()
  }
}