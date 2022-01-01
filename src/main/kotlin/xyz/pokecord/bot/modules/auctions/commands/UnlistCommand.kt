package xyz.pokecord.bot.modules.auctions.commands

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.Confirmation
import xyz.pokecord.utils.withCoroutineLock

object UnlistCommand : Command() {
  override val name = "Unlist"
  override var aliases = arrayOf("withdraw", "remove", "delete")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument auctionId: Int?
  ) {
    if (!context.hasStarted(true)) return

    if (auctionId == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.auctions.commands.unlist.errors.noAuctionId")
        ).build()
      ).queue()
      return
    }

    context.bot.cache.getAuctionLock(auctionId).withCoroutineLock {
      val auction = context.bot.database.auctionRepository.getAuction(auctionId)
      if (auction == null) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.auctions.commands.unlist.errors.noAuctionFound")
          ).build()
        ).queue()
        return@withCoroutineLock
      } else if (auction.ownerId != context.author.id) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.auctions.commands.unlist.errors.notYourAuction")
          ).build()
        ).queue()
        return@withCoroutineLock
      } else if (auction.bids.isNotEmpty()) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.auctions.commands.unlist.errors.bidsActive")
          ).build()
        ).queue()
        return@withCoroutineLock
      } else if(auction.ended) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.auctions.commands.unlist.errors.auctionEnded")
          ).build()
        ).queue()
        return@withCoroutineLock
      }

      val pokemon = context.bot.database.pokemonRepository.getPokemonById(auction.pokemon)

      if (pokemon != null) {
        val confirmation = Confirmation(context, context.author.id)
        val confirmed = confirmation.result(
          context.embedTemplates.confirmation(
            context.translate(
              "modules.auctions.commands.unlist.confirmation.description",
              mapOf(
                "pokemonIV" to pokemon.ivPercentage,
                "pokemonName" to context.translator.pokemonDisplayName(pokemon)
              )
            ),
            context.translate("modules.auctions.commands.unlist.confirmation.title")
          )
        )

        if (confirmed) {
          val session = context.bot.database.startSession()

          session.use {
            session.startTransaction()
            context.bot.database.pokemonRepository.updateOwnerId(pokemon._id, context.author.id)
            context.bot.database.auctionRepository.endAuction(auction)
            session.commitTransactionAndAwait()
          }

          context.reply(
            context.embedTemplates.normal(
              context.translate(
                "modules.auctions.commands.unlist.confirmed.description",
                mapOf(
                  "pokemonIV" to pokemon.ivPercentage,
                  "pokemonName" to context.translator.pokemonDisplayName(pokemon)
                )
              ),
              context.translate("modules.auctions.commands.unlist.confirmed.title")
            ).build()
          ).queue()
        }
      } else {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.auctions.commands.unlist.errors.noPokemonFound")
          ).build()
        ).queue()
      }
    }
  }
}
