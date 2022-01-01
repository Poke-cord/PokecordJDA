package xyz.pokecord.bot.modules.auctions.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.utils.TimeFormat
import org.litote.kmongo.eq
import org.litote.kmongo.match
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.Auction
import xyz.pokecord.bot.core.structures.discord.EmbedTemplates
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.utils.EmbedPaginator
import kotlin.math.ceil

object AuctionsCommand: ParentCommand() {
  override val name = "Auctions"
  override var aliases = arrayOf("ah", "auction")
  override val childCommands: MutableList<Command> = mutableListOf(ListCommand, UnlistCommand, BidCommand, InfoCommand, ProfileCommand, NotifyCommand)

  suspend fun formatAuctions(
    context: ICommandContext,
    auctions: List<Auction>,
    showBids: Boolean = false,
  ): String {
    val desc = auctions.map {
      val auctionPokemon = context.bot.database.pokemonRepository.getPokemonById(it.pokemon)
      if(auctionPokemon != null) {
        val pokemonIv = auctionPokemon.ivPercentage
        val pokemonName = context.translator.pokemonName(auctionPokemon)

        val highestBid = it.highestBid
        val outbid = if(highestBid != null) highestBid.userId != context.author.id else false
        val bidStatus = if (highestBid != null) {
          if (showBids && !outbid) "" else "Top Bid ${context.translator.numberFormat(highestBid.amount)}"
        } else "Starting Bid: ${context.translator.numberFormat(it.startingBid)}"
        val outbidStatus = if(showBids && outbid) " | Outbid" else ""

        if(it.timeLeft > 0 && !it.ended) {
          "`${it.id}` IV **$pokemonIv $pokemonName**$outbidStatus | $bidStatus |  Ends ${TimeFormat.RELATIVE.after(it.timeLeft)}"
        } else null
      } else null
    }
    return desc.filterNotNull().joinToString("\n")
  }

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(optional = true) bids: String?,
    @Argument(optional = true) page: Int?,
  ) {
    if(!context.hasStarted(true)) return

    val templateEmbedBuilder =
      EmbedBuilder()
        .setTitle(
          context.translate(
            "modules.auctions.commands.auctions.embeds.title"
          )
        )
        .setColor(EmbedTemplates.Color.GREEN.code)

    val aggregation = arrayListOf(match(Auction::ended eq false))
    val count = context.bot.database.auctionRepository.getAuctionCount(aggregation = aggregation)
    if (count < 1) {
      context.reply(
        templateEmbedBuilder.setDescription(context.translate("modules.auctions.commands.auctions.errors.noResults"))
          .setColor(EmbedTemplates.Color.RED.code).build()
      ).queue()
      return
    }

    val pageCount = ceil((count.toDouble() / 10)).toInt()
    val paginator = EmbedPaginator(context, pageCount, { pageIndex ->
      if (pageIndex >= pageCount) {
        return@EmbedPaginator templateEmbedBuilder.setDescription(context.translate("modules.auctions.commands.auctions.errors.noResults"))
          .setColor(EmbedTemplates.Color.RED.code).setFooter("")
      }
      val auctionsList = context.bot.database.auctionRepository.getAuctionList(skip = pageIndex * 10, aggregation = aggregation)
      templateEmbedBuilder.clearFields().setFooter(null)
      templateEmbedBuilder.setDescription(
        formatAuctions(
          context,
          auctionsList,
          bids == "b" || bids == "bid" || bids == "bids"
        )
      )
      templateEmbedBuilder
    }, if (page == null) 0 else page - 1)
    paginator.start()
  }
}