package xyz.pokecord.bot.modules.market.commands

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
    @Argument listingId: Int?
  ) {
    if (!context.hasStarted(true)) return

    if (listingId == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.market.commands.unlist.errors.noListingId")
        ).build()
      ).queue()
      return
    }

    context.bot.cache.getMarketLock(listingId).withCoroutineLock(30) {
      val listing = context.bot.database.marketRepository.getListing(listingId)
      if (listing == null || listing.sold || listing.unlisted) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.market.commands.unlist.errors.noListingFound")
          ).build()
        ).queue()
        return@withCoroutineLock
      } else if (listing.ownerId != context.author.id) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.market.commands.unlist.errors.notYourListing")
          ).build()
        ).queue()
        return@withCoroutineLock
      }

      val pokemon = context.bot.database.pokemonRepository.getPokemonById(listing.pokemon)

      if (pokemon != null) {
        val confirmation = Confirmation(context)
        val confirmed = confirmation.result(
          context.embedTemplates.confirmation(
            context.translate(
              "modules.market.commands.unlist.confirmation.description",
              mapOf(
                "pokemonIV" to pokemon.ivPercentage,
                "pokemonName" to context.translator.pokemonDisplayName(pokemon)
              )
            ),
            context.translate("modules.market.commands.unlist.confirmation.title")
          )
        )

        if (confirmed) {
          val session = context.bot.database.startSession()

          session.use {
            session.startTransaction()
            val userData = context.bot.database.userRepository.getUser(context.author.id)
            context.bot.database.pokemonRepository.updateOwnerId(pokemon, context.author.id, session)
            context.bot.database.marketRepository.markUnlisted(listing, session) // Still deciding
            context.bot.database.userRepository.updatePokemonCount(userData, userData.pokemonCount + 1, session)
            session.commitTransactionAndAwait()
          }

          context.reply(
            context.embedTemplates.normal(
              context.translate(
                "modules.market.commands.unlist.confirmed.description",
                mapOf(
                  "pokemonIV" to pokemon.ivPercentage,
                  "pokemonName" to context.translator.pokemonDisplayName(pokemon)
                )
              ),
              context.translate("modules.market.commands.unlist.confirmed.title")
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
