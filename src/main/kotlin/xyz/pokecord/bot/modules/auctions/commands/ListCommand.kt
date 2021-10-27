package xyz.pokecord.bot.modules.auctions.commands

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.Auction
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.Config
import xyz.pokecord.bot.utils.Confirmation
import xyz.pokecord.bot.utils.PokemonResolvable
import xyz.pokecord.bot.utils.extensions.humanizeMs
import xyz.pokecord.bot.utils.extensions.parseTime
import xyz.pokecord.utils.withCoroutineLock

object ListCommand : Command() {
  override val name = "List"
  override var aliases = arrayOf("create")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument pokemonRes: PokemonResolvable?,
    @Argument startingBid: Int?,
    @Argument time: String?
  ) {
    if (!context.hasStarted(true)) return
    if (context.getTradeState() != null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.auctions.commands.list.errors.inTrade")
        ).build()
      ).queue()
      return
    }

    val userData = context.getUserData()
    val pokemon = context.resolvePokemon(context.author, userData, pokemonRes)
    if (pokemon == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.auctions.commands.list.errors.noPokemonProvided")
        ).build()
      ).queue()
      return
    }

    if (startingBid != null && startingBid < 10 && startingBid > 10000000) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.auctions.commands.list.errors.startingBidPrice")
        ).build()
      ).queue()
      return
    }

    val endingTimestamp = 4 * 60 * 60 * 1000

    val transferable = pokemon.transferable(context.bot.database)
    if (transferable != OwnedPokemon.TransferStates.SUCCESS) {
      context.reply(
        context.embedTemplates.error(transferable.errMessage).build()
      ).queue()
      return
    }

    val confirmation = Confirmation(context, context.author.id)
    val confirmed = confirmation.result(
      context.embedTemplates.confirmation(
        context.translate(
          "modules.auctions.commands.list.confirmation.description",
          mapOf(
            "pokemonIV" to pokemon.ivPercentage,
            "pokemonName" to context.translator.pokemonDisplayName(pokemon)
          )
        ),
        context.translate("modules.auctions.commands.list.confirmation.title")
      )
    )

    if (confirmed) {
      val auctionTime = time?.parseTime() ?: Config.defaultAuctionTime
      context.bot.cache.getAuctionIdLock().withCoroutineLock {
        val session = context.bot.database.startSession()
        session.use {
          session.startTransaction()
          val latestAuctionId = context.bot.database.auctionRepository.getLatestAuction(session)?.id ?: 0
          context.bot.database.auctionRepository.createAuction(
            Auction(
              latestAuctionId + 1,
              pokemon.ownerId,
              pokemon._id,
              auctionTime,
              _isNew = true
            ),
            session
          )
          context.bot.database.pokemonRepository.updateOwnerId(pokemon._id, "auction-pokemon-holder", session)
          session.commitTransactionAndAwait()
        }
      }

      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.auctions.commands.list.confirmed.description",
            mapOf(
              "pokemonIV" to pokemon.ivPercentage,
              "pokemonName" to context.translator.pokemonDisplayName(pokemon),
              "formattedDate" to auctionTime.humanizeMs(),
              "startingBid" to startingBid.toString()
            )
          ),
          context.translate("modules.auctions.commands.list.confirmed.title"),
        ).build()
      ).queue()
    }
  }
}