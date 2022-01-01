package xyz.pokecord.bot.modules.auctions.commands

import dev.minn.jda.ktx.await
import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.Bid
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.modules.auctions.tasks.AuctionTask
import xyz.pokecord.bot.utils.Confirmation
import xyz.pokecord.utils.withCoroutineLock

object BidCommand : Command() {
  override val name = "bid"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument auctionId: Int?,
    @Argument bidAmount: Int?
  ) {
    if (!context.hasStarted(true)) return

    if (auctionId == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.auctions.commands.bid.errors.noAuctionId")
        ).build()
      ).queue()
      return
    }

    context.bot.cache.getAuctionLock(auctionId).withCoroutineLock {
      val auction = context.bot.database.auctionRepository.getAuction(auctionId)
      if (auction == null) {
        context.reply(
          context.embedTemplates.error(
            context.translate(
              "modules.auctions.commands.bid.errors.noAuctionFound",
              "id" to auctionId.toString()
            )
          ).build()
        ).queue()
        return@withCoroutineLock
      } else if (auction.ownerId == context.author.id) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.auctions.commands.bid.errors.selfBid")
          ).build()
        ).queue()
        return@withCoroutineLock
      }

      val pokemon = context.bot.database.pokemonRepository.getPokemonById(auction.pokemon)
      if (pokemon == null) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.auctions.commands.bid.errors.noPokemonFound")
          ).build()
        ).queue()
        return@withCoroutineLock
      }

      if (bidAmount == null) {
        context.reply(
          context.embedTemplates.error(
            context.translate(
              "modules.auctions.commands.bid.errors.noBidAmount",
              mapOf(
                "pokemonIV" to pokemon.ivPercentage,
                "pokemonName" to context.translator.pokemonDisplayName(pokemon)
              )
            )
          ).build()
        ).queue()
        return@withCoroutineLock
      }

      val userData = context.getUserData()
      val highestBid = auction.highestBid
      if (highestBid != null) {
        if (userData.credits < highestBid.amount) {
          context.reply(
            context.embedTemplates.error(
              context.translate(
                "modules.auctions.commands.bid.errors.notEnoughCredits",
                "bid" to highestBid.amount.toString()
              )
            ).build()
          ).queue()
          return@withCoroutineLock
        } else if (highestBid.userId == context.author.id) {
          context.reply(
            context.embedTemplates.error(
              context.translate(
                "modules.auctions.commands.bid.errors.topBid",
                "bid" to highestBid.amount.toString()
              )
            ).build()
          ).queue()
          return@withCoroutineLock
        } else if (highestBid.amount + auction.bidIncrement > bidAmount) {
          context.reply(
            context.embedTemplates.error(
              context.translate(
                "modules.auctions.commands.bid.errors.bidTooLow",
                mapOf(
                  "bid" to (highestBid.amount + auction.bidIncrement).toString(),
                  "bidIncrement" to auction.bidIncrement.toString()
                )
              )
            ).build()
          ).queue()
          return@withCoroutineLock
        }
      } else {
        if (auction.startingBid > bidAmount) {
          context.reply(
            context.embedTemplates.error(
              context.translate(
                "modules.auctions.commands.bid.errors.bidTooLow",
                mapOf(
                  "bid" to auction.startingBid.toString(),
                  "bidIncrement" to auction.bidIncrement.toString()
                )
              )
            ).build()
          ).queue()
          return@withCoroutineLock
        }
      }

      val confirmation = Confirmation(context, context.author.id)
      val confirmed = confirmation.result(
        context.embedTemplates.confirmation(
          context.translate(
            "modules.auctions.commands.bid.confirmation.description",
            mapOf(
              "bid" to bidAmount.toString(),
              "pokemonIV" to pokemon.ivPercentage,
              "pokemonName" to context.translator.pokemonDisplayName(pokemon)
            )
          ),
          context.translate("modules.auctions.commands.bid.confirmation.title")
        )
      )

      if (confirmed) {
        val session = context.bot.database.startSession()
        session.use {
          session.startTransaction()

          if(highestBid != null) {
            val highestBidUserData = context.bot.database.userRepository.getUser(highestBid.userId)
            val highestBidUser = context.jda.retrieveUserById(highestBid.userId).await()

            context.bot.database.userRepository.incCredits(highestBidUserData, bidAmount, session)

            highestBidUser.openPrivateChannel().await().sendMessageEmbeds(
              context.embedTemplates.normal(
                context.translate(
                  "modules.auctions.commands.bid.outbid.description",
                  mapOf(
                    "bidder" to context.author.asMention,
                    "ID" to auction.id.toString(),
                    "pokemonName" to context.translator.pokemonDisplayName(pokemon),
                    "bidDifference" to (highestBid.amount - bidAmount).toString()
                  )
                ),
                context.translate("modules.auctions.commands.bid.outbid.title")
              ).build()
            ).queue()
          }

          context.bot.database.userRepository.incCredits(userData, -bidAmount, session)
          context.bot.database.auctionRepository.insertBid(auction, Bid(context.author.id, bidAmount))
          session.commitTransactionAndAwait()
        }

        context.reply(
          context.embedTemplates.normal(
            context.translate(
              "modules.auctions.commands.bid.confirmed.description",
              mapOf(
                "bid" to bidAmount.toString(),
                "pokemonIV" to pokemon.ivPercentage,
                "pokemonName" to context.translator.pokemonDisplayName(pokemon)
              )
            ),
            context.translate("modules.auctions.commands.bid.confirmed.title")
          ).build()
        ).queue()

        val owner = context.jda.retrieveUserById(auction.ownerId).await()
        val ownerData = context.bot.database.userRepository.getUser(owner.id)
        if(owner != null && ownerData.bidNotifications) {
          val ownerDmChannel = owner.openPrivateChannel().await()
          ownerDmChannel.sendMessageEmbeds(
            context.embedTemplates.normal(
              context.translate("modules.auctions.commands.bid.bidNotification.description",
                mapOf("bidderTag" to context.author.asTag,
                  "amount" to bidAmount.toString(),
                  "pokemonIV" to pokemon.ivPercentage,
                  "pokemonName" to context.translator.pokemonDisplayName(pokemon)
                )
              ),
              context.translate("modules.auctions.commands.bid.bidNotification.title")
            ).build()
          ).queue()
        }
      }
    }
  }
}
