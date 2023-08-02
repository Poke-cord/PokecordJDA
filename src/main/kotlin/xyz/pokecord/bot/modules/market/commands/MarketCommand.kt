package xyz.pokecord.bot.modules.market.commands

import net.dv8tion.jda.api.EmbedBuilder
import org.litote.kmongo.`in`
import org.litote.kmongo.eq
import org.litote.kmongo.match
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.Listing
import xyz.pokecord.bot.core.managers.database.repositories.PokemonRepository
import xyz.pokecord.bot.core.structures.discord.EmbedTemplates
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.utils.EmbedPaginator
import kotlin.math.ceil

object MarketCommand : ParentCommand() {
  override val name = "Market"
  override var aliases = arrayOf("markets", "mk")
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
        val pokemonLevel = listingPokemon.level
        val pokemonName = context.translator.pokemonDisplayName(listingPokemon, false)
        "|`${it.id}`| **$pokemonName** │ LVL $pokemonLevel - IV $pokemonIv │ **${context.translator.numberFormat(it.price)}c**"
      } else null
    }

    return desc.filterNotNull().joinToString("\n")
  }

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(aliases = ["sh"], prefixed = true, optional = true) shiny: Boolean?,
    @Argument(aliases = ["n"], prefixed = true, optional = true) nature: String?,
    @Argument(
      aliases = ["r"],
      prefixed = true,
      optional = true
    ) rarity: String?,
    @Argument(aliases = ["t"], prefixed = true, optional = true) type: String?,
    @Argument(aliases = ["re"], prefixed = true, optional = true) regex: Regex?,
    @Argument(optional = true) page: Int?,
    @Argument(
      aliases = ["s"],
      name = "search",
      consumeRest = true,
      prefixed = true,
      optional = true
    ) searchQuery: String?,
  ) {
    if (!context.hasStarted(true)) return

    val templateEmbedBuilder =
      EmbedBuilder()
        .setTitle(
          context.translate("modules.market.commands.market.embeds.title")
        )
        .setColor(EmbedTemplates.Color.GREEN.code)

    val searchOptions =
      PokemonRepository.PokemonSearchOptions(
        order = null,
        favorites = null,
        nature,
        rarity,
        shiny,
        type,
        regex,
        searchQuery
      ) // order = null because it's not supported yet and favorites = null because they only really apply to specific user's collections

    val filters = mutableListOf(
      Listing::sold eq false,
      Listing::unlisted eq false
    )

    val matchingPokemon =
      if (searchOptions.hasOptions) {
        module.bot.database.pokemonRepository.getPokemonIds(
          "market-pokemon-holder",
          limit = null,
          searchOptions = searchOptions
        )
          .map { it._id }
      } else null
    if (matchingPokemon != null) {
      filters.add(Listing::pokemon `in` matchingPokemon)
    }

    val aggregation = listOf(match(*filters.toTypedArray()))

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
      val listings = context.bot.database.marketRepository.getListings(
        skip = pageIndex * 10,
        aggregation = aggregation.toMutableList()
      )
      templateEmbedBuilder.clearFields().setFooter(null)
      templateEmbedBuilder.setDescription(formatListings(context, listings))
      templateEmbedBuilder
    }, if (page == null) 0 else page - 1)
    paginator.start()
  }
}
