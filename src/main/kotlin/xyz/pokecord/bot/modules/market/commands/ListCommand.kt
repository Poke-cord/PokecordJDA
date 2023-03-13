package xyz.pokecord.bot.modules.market.commands

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.Listing
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.Confirmation
import xyz.pokecord.bot.utils.PokemonResolvable
import xyz.pokecord.utils.withCoroutineLock

object ListCommand : Command() {
  override val name = "List"
  override var aliases = arrayOf("create")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument pokemonRes: PokemonResolvable?,
    @Argument price: Int?,
  ) {
    if (!context.hasStarted(true)) return
    if (context.getTradeState() != null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.market.commands.list.errors.inTrade")
        ).build()
      ).queue()
      return
    }

    if (pokemonRes == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.market.commands.list.errors.noPokemonProvided")
        ).build()
      ).queue()
      return
    }

    val userData = context.getUserData()
    val pokemon = context.resolvePokemon(context.author, userData, pokemonRes)
    if (pokemon == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("misc.errors.pokemonNotFound")
        ).build()
      ).queue()
      return
    }

    if (price == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.market.commands.list.errors.noPriceProvided")
        ).build()
      ).queue()
      return
    } else if (price !in 9..10000001) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.market.commands.list.errors.startingBidPrice")
        ).build()
      ).queue()
      return
    }

    val transferable = pokemon.transferable(context.bot.database)
    if (transferable != OwnedPokemon.TransferStates.SUCCESS) {
      context.reply(
        context.embedTemplates.error(transferable.errMessage).build()
      ).queue()
      return
    }

    val confirmation = Confirmation(context)
    val confirmed = confirmation.result(
      context.embedTemplates.confirmation(
        context.translate(
          "modules.market.commands.list.confirmation.description",
          mapOf(
            "pokemonIV" to pokemon.ivPercentage,
            "pokemonName" to context.translator.pokemonDisplayName(pokemon),
            "price" to price.toString()
          )
        ),
        context.translate("modules.market.commands.list.confirmation.title")
      )
    )

    if (confirmed) {
      context.bot.cache.getMarketIdLock().withCoroutineLock(30) {
        val session = context.bot.database.startSession()
        session.use {
          session.startTransaction()
          val latestMarketId = context.bot.database.marketRepository.getLatestListing(session)?.id ?: 0
          context.bot.database.marketRepository.createListing(
            Listing(
              latestMarketId + 1,
              pokemon.ownerId,
              pokemon._id,
              price,
              _isNew = true
            ),
            session
          )
          context.bot.database.userRepository.updatePokemonCount(userData, userData.pokemonCount - 1, session)
          context.bot.database.pokemonRepository.updateOwnerId(pokemon, "market-pokemon-holder", session)
          session.commitTransactionAndAwait()
        }
      }

      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.market.commands.list.confirmed.description",
            mapOf(
              "pokemonIV" to pokemon.ivPercentage,
              "pokemonName" to context.translator.pokemonDisplayName(pokemon),
              "price" to price.toString()
            )
          ),
          context.translate("modules.market.commands.list.confirmed.title"),
        ).build()
      ).queue()
    }
  }
}
