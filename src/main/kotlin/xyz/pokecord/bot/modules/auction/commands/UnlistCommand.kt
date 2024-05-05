package xyz.pokecord.bot.modules.auction.commands

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
          context.translate("modules.auction.errors.general.noAuctionId")
        ).build()
      ).queue()
      return
    }

    context.bot.cache.getAuctionLock(auctionId).withCoroutineLock(30) {
      val auction = context.bot.database.auctionRepository.getAuction(auctionId)
      if (auction == null) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.auction.errors.general.noAuctionFound")
          ).build()
        ).queue()
        return@withCoroutineLock
      } else if (auction.ownerId != context.author.id) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.auction.errors.unlist.notYourAuction")
          ).build()
        ).queue()
        return@withCoroutineLock
      } else if (auction.bids.isNotEmpty()) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.auction.errors.unlist.bidsActive")
          ).build()
        ).queue()
        return@withCoroutineLock
      } else if(auction.ended) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.auction.errors.general.auctionEnded")
          ).build()
        ).queue()
        return@withCoroutineLock
      }

      val pokemon = context.bot.database.pokemonRepository.getPokemonById(auction.pokemon)

      if (pokemon != null) {
        val confirmation = Confirmation(context)
        val confirmed = confirmation.result(
          context.embedTemplates.confirmation(
            context.translate(
              "modules.auction.embeds.unlist.confirmation.description",
              mapOf(
                "pokemonIV" to pokemon.ivPercentage,
                "pokemonName" to context.translator.pokemonDisplayName(pokemon)
              )
            ),
            context.translate("modules.auction.embeds.unlist.confirmation.title")
          )
        )

        if (confirmed) {
          val session = context.bot.database.startSession()

          session.use {
            session.startTransaction()
            val userData = context.bot.database.userRepository.getUser(context.author.id)
            context.bot.database.pokemonRepository.updateOwnerId(pokemon, context.author.id, session)
            context.bot.database.auctionRepository.endAuction(auction, session)
            context.bot.database.userRepository.updatePokemonCount(userData, userData.pokemonCount + 1, session)
            session.commitTransactionAndAwait()
          }

          context.reply(
            context.embedTemplates.success(
              context.translate(
                "modules.auction.embeds.unlist.confirmed.description",
                mapOf(
                  "pokemonIV" to pokemon.ivPercentage,
                  "pokemonName" to context.translator.pokemonDisplayName(pokemon)
                )
              ),
              context.translate("modules.auction.embeds.unlist.confirmed.title")
            ).build()
          ).queue()
        }
      } else {
        context.reply(
          context.embedTemplates.error(
            context.translate("misc.errors.pokemonDoesNotExist")
          ).build()
        ).queue()
      }
    }
  }
}
