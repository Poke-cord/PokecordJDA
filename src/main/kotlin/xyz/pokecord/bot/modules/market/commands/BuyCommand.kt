package xyz.pokecord.bot.modules.market.commands

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.Confirmation
import xyz.pokecord.utils.withCoroutineLock

object BuyCommand : Command() {
  override val name = "buy"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument listingId: Int?,
  ) {
    if (!context.hasStarted(true)) return

    if (listingId == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.market.commands.buy.errors.noListingId")
        ).build()
      ).queue()
    } else {
      context.bot.cache.getMarketLock(listingId).withCoroutineLock {
        val listing = context.bot.database.marketRepository.getListing(listingId)
        if (listing == null) {
          context.reply(
            context.embedTemplates.error(
              context.translate(
                "modules.market.commands.buy.errors.noListingFound",
                "id" to listingId.toString()
              )
            ).build()
          ).queue()
          return@withCoroutineLock
        } else if (listing.ownerId == context.author.id) {
          context.reply(
            context.embedTemplates.error(
              context.translate("modules.market.commands.buy.errors.selfBuy")
            ).build()
          ).queue()
          return@withCoroutineLock
        }

        val pokemon = context.bot.database.pokemonRepository.getPokemonById(listing.pokemon)
        if (pokemon == null) {
          context.reply(
            context.embedTemplates.error(
              context.translate("modules.market.commands.buy.errors.noPokemonFound")
            ).build()
          ).queue()
          return@withCoroutineLock
        }

        val userData = context.getUserData()
        if(listing.price > userData.credits) {
          context.reply(
            context.embedTemplates.error(
              context.translate("modules.market.commands.buy.errors.notEnoughCredits")
            ).build()
          ).queue()
        } else {
          val confirmation = Confirmation(context, context.author.id)
          val confirmed = confirmation.result(
            context.embedTemplates.confirmation(
              context.translate(
                "modules.market.commands.buy.confirmation.description",
                mapOf(
                  "price" to listing.price.toString(),
                  "pokemonIV" to pokemon.ivPercentage,
                  "pokemonName" to context.translator.pokemonDisplayName(pokemon)
                )
              ),
              context.translate("modules.market.commands.buy.confirmation.title")
            )
          )

          if (confirmed) {
            val session = context.bot.database.startSession()

            session.use {
              session.startTransaction()
              context.bot.database.userRepository.incCredits(userData, -listing.price, session)
              context.bot.database.marketRepository.markSold(listing)
              context.bot.database.pokemonRepository.updateOwnerId(pokemon._id, context.author.id, session)
              session.commitTransactionAndAwait()
            }

            context.reply(
              context.embedTemplates.normal(
                context.translate(
                  "modules.market.commands.buy.confirmed.description",
                  mapOf(
                    "price" to listing.price.toString(),
                    "pokemonIV" to pokemon.ivPercentage,
                    "pokemonName" to context.translator.pokemonDisplayName(pokemon)
                  )
                ),
                context.translate("modules.market.commands.buy.confirmed.title")
              ).build()
            ).queue()
          }
        }
      }
    }
  }
}
