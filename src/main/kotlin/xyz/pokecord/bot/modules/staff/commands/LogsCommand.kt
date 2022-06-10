package xyz.pokecord.bot.modules.staff.commands

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.litote.kmongo.Id
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.or
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.*
import xyz.pokecord.bot.modules.staff.StaffCommand
import java.text.SimpleDateFormat
import java.util.*

object LogsCommand : StaffCommand() {
  override val name = "Logs"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument userId: String?
  ) {
    if (userId == null) {
      context.reply(context.embedTemplates.error("gib user id pls").build()).queue()
      return
    }

    val logRetriever = LogRetriever(userId, module.bot.database)
    val auctionLogs = logRetriever.getAuctionLogs().ifEmpty { "None" }
    val giftLogs = logRetriever.getGiftLogs().ifEmpty { "None" }
    val marketLogs = logRetriever.getMarketLogs().ifEmpty { "None" }
    val tradeLogs = logRetriever.getTradeLogs().ifEmpty { "None" }
    context
      .addAttachment(auctionLogs.toByteArray(), "auction.txt")
      .addAttachment(giftLogs.toByteArray(), "gift.txt")
      .addAttachment(marketLogs.toByteArray(), "market.txt")
      .addAttachment(tradeLogs.toByteArray(), "trade.txt")
      .reply("Here are the logs for ${userId}!")
      .queue()
  }

  class LogRetriever(
    private val targetUserId: String,
    private val database: Database
  ) {
    private val pokemonCache = mutableMapOf<Id<OwnedPokemon>, String>()
    private val userTagCache = mutableMapOf<String, String>()
    private var sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").apply {
      timeZone = TimeZone.getTimeZone("UTC")
    }

    private suspend fun getPokemon(id: Id<OwnedPokemon>): String {
      return pokemonCache.getOrPut(id) {
        database.pokemonRepository.getPokemonById(id)?.let {
          "${it.ivPercentage} IV ${it.displayName} [${id}]"
        } ?: id.toString()
      }
    }

    private suspend fun getUser(id: String): String {
      return userTagCache.getOrPut(id) {
        database.userRepository.getUser(id).let {
          "${it.tag} [${it.id}]"
        }
      }
    }

    private fun <T> Id<T>.toISODate(): String {
      return sdf.format(ObjectId(this.toString()).date)
    }

    suspend fun getAuctionLogs(): String {
      val auctions = database.auctionCollection
        .find(
          or(
            Auction::ownerId eq targetUserId,
            Auction::bids / Bid::userId eq targetUserId
          )
        ).toFlow()

      return auctions.map {
        val lines = mutableListOf<String>()

        val owner = getUser(it.ownerId)
        val winner = it.highestBid?.userId?.let { id -> getUser(id) }
        val pokemon = getPokemon(it.pokemon)
        lines.add("Auction #${it.id}")
        lines.add("Bids")
        lines.addAll(
          it.bids.map { bid ->
            val bidder = getUser(bid.userId)
            "${bid.amount} by $bidder".prependIndent("  ")
          }
        )
        lines.add("From $owner to $winner - $pokemon highest bid at ${it.highestBid?._id?.toISODate() ?: "N/A"}")
        lines.joinToString("\n")
      }
        .toList()
        .joinToString("\n\n")
    }

    suspend fun getGiftLogs(): String {
      val gifts = database.giftCollection.find(
        or(
          Gift::senderId eq targetUserId,
          Gift::receiverId eq targetUserId
        )
      ).toFlow()

      return gifts.map {
        val sender = getUser(it.senderId)
        val receiver = getUser(it.receiverId)

        if (it.credits > 0) {
          "${it.credits} credits from $sender to $receiver at ${it._id.toISODate()}"
        } else if (it.pokemonIds.isNotEmpty()) {
          it.pokemonIds.map { pokemonId ->
            val pokemon = getPokemon(pokemonId)
            "From $sender to $receiver - $pokemon at ${it._id.toISODate()} - $pokemonId"
          }.joinToString("\n")
        } else null
      }
        .filterNotNull()
        .toList()
        .joinToString("\n\n")
    }

    suspend fun getMarketLogs(): String {
      val marketListings = database.marketCollection.find(
        or(
          Listing::ownerId eq targetUserId,
          Listing::soldTo eq targetUserId
        )
      ).toFlow()

      return marketListings.map {
        val owner = getUser(it.ownerId)
        val buyer = it.soldTo?.let { buyerId -> getUser(buyerId) }
        val pokemon = getPokemon(it.pokemon)

        "[${it.id}] - From $owner to $buyer - $pokemon for ${it.price}"
      }
        .toList()
        .joinToString("\n\n")
    }

    suspend fun getTradeLogs(): String {
      val trades = database.tradeCollection.find(
        or(
          Trade::initiator / TraderData::userId eq targetUserId,
          Trade::receiver / TraderData::userId eq targetUserId
        )
      ).toFlow()

      return trades.map {
        val lines = mutableListOf<String>()

        val initiator = getUser(it.initiator.userId)
        val receiver = getUser(it.receiver.userId)

        lines.add("Trade ${it._id} at ${it._id.toISODate()}")

        suspend fun getTraderLogs(trader: TraderData, traderTitle: String, traderNameOrId: String): List<String> {
          val traderLogLines = mutableListOf<String>()
          traderLogLines.add(traderTitle.prependIndent("  "))
          traderLogLines.add("    Credits: ${trader.credits}")
          traderLogLines.add("    Pokemon")
          traderLogLines.addAll(
            trader.pokemon.map { pokemonId ->
              val pokemon = getPokemon(pokemonId)
              "From $traderNameOrId - $pokemon"
            }
          )
          return traderLogLines
        }

        lines.addAll(getTraderLogs(it.initiator, "Initiator", initiator))
        lines.addAll(getTraderLogs(it.initiator, "Receiver", receiver))

        lines.joinToString("\n")
      }
        .toList()
        .joinToString("\n\n")
    }
  }
}
