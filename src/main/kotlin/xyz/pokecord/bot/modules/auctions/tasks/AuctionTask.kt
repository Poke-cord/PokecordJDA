package xyz.pokecord.bot.modules.auctions.tasks

import dev.minn.jda.ktx.await
import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.core.structures.discord.EmbedTemplates
import xyz.pokecord.bot.core.structures.discord.base.Task

object AuctionTask : Task() {
  override val name = "AuctionChecker"
  override val interval = 30 * 1000L

  private val embedTemplates = EmbedTemplates()

  override suspend fun execute() {
    val endedAuctions = module.bot.database.auctionRepository.auctionTask(interval, module.bot.shardManager)
    for (endedAuction in endedAuctions) {
      val pokemon = module.bot.database.pokemonRepository.getPokemonById(endedAuction.pokemon)
      if (pokemon != null) {
        if (endedAuction.bids.isNotEmpty()) {
          val highestBid = endedAuction.highestBid!!
          val user = module.bot.database.userRepository.getUser(endedAuction.ownerId)

          val session = module.bot.database.startSession()
          session.use {
            session.startTransaction()
            val userData = module.bot.database.userRepository.getUser(highestBid.userId)
            module.bot.database.pokemonRepository.updateOwnerId(pokemon, highestBid.userId, session)
            module.bot.database.userRepository.incCredits(user, highestBid.amount, session)
            module.bot.database.userRepository.updatePokemonCount(userData, userData.pokemonCount + 1, session)
            session.commitTransactionAndAwait()
          }

          try {
            val jdaOwnerChannel = module.bot.shardManager
              .retrieveUserById(endedAuction.ownerId).await()
              .openPrivateChannel().await()

            val jdaWinnerChannel = module.bot.shardManager
              .retrieveUserById(highestBid.userId).await()
              .openPrivateChannel().await()

            jdaOwnerChannel.sendMessageEmbeds(
              embedTemplates.normal(
                embedTemplates.translate(
                  "modules.auctions.tasks.checker.auctionSold.description",
                  mapOf(
                    "pokemon" to pokemon.displayName,
                    "amount" to highestBid.amount.toString()
                  )
                ),
                embedTemplates.translate("modules.auctions.tasks.checker.auctionSold.title")
              ).build()
            ).queue()

            jdaWinnerChannel.sendMessageEmbeds(
              embedTemplates.normal(
                embedTemplates.translate(
                  "modules.auctions.tasks.checker.auctionWon.description",
                  mapOf(
                    "pokemon" to pokemon.displayName,
                    "amount" to highestBid.amount.toString()
                  )
                ),
                embedTemplates.translate("modules.auctions.tasks.checker.auctionWon.title")
              ).build()
            ).queue()
          } catch (_: Exception) {
          }
        } else {
          val session = module.bot.database.startSession()
          session.use {
            session.startTransaction()
            val userData = module.bot.database.userRepository.getUser(endedAuction.ownerId)
            module.bot.database.userRepository.updatePokemonCount(userData, userData.pokemonCount + 1, session)
            module.bot.database.pokemonRepository.updateOwnerId(pokemon, endedAuction.ownerId, session)
            session.commitTransactionAndAwait()
          }

          try {
            val jdaOwnerChannel = module.bot.shardManager
              .retrieveUserById(endedAuction.ownerId).await()
              .openPrivateChannel().await()

            jdaOwnerChannel.sendMessageEmbeds(
              embedTemplates.normal(
                embedTemplates.translate(
                  "modules.auctions.tasks.checker.auctionEnded.description",
                  "pokemon" to pokemon.displayName
                ),
                embedTemplates.translate("modules.auctions.tasks.checker.auctionEnded.title")
              ).build()
            ).queue()
          } catch (_: Exception) {
          }
        }
      }
    }
  }
}