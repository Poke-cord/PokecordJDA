package xyz.pokecord.bot.modules.release.commands

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.items.EVItem
import xyz.pokecord.bot.modules.trading.TradingModule
import xyz.pokecord.bot.utils.Confirmation

object ReleaseConfirmCommand : Command() {
  override val name: String = "confirm"

  @Executor
  suspend fun execute(
    context: ICommandContext
  ) {
    if (!context.hasStarted(true)) return

    val releaseState = context.getTradeState()
    if (releaseState == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.pokemon.commands.release.errors.notInRelease")
        ).build()
      ).queue()
      return
    }

    if (
      releaseState.initiator.pokemon.isEmpty()
    ) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.pokemon.commands.release.embeds.confirmation.errors.emptyRelease")
        ).build()
      ).queue()
      return
    }

    val authorReleaseData = releaseState.initiator
    val authorUserData = context.bot.database.userRepository.getUser(authorReleaseData.userId)
    val authorPokemon = context.bot.database.pokemonRepository.getPokemonByIds(authorReleaseData.pokemon)

    val authorPokemonText =
      TradingModule.getTradeStatePokemonText(context, authorPokemon, authorPokemon.map { it.id }, false)
    val confirmation = Confirmation(context)
    val result =
      confirmation.result(
        context.embedTemplates.confirmation(
          context.translate(
            "modules.pokemon.commands.release.embeds.confirmation.embed.description",
            mapOf(
              "pokemon" to authorPokemonText.joinToString("\n").ifEmpty { "None" }
            )
          ),
          context.translate("modules.pokemon.commands.release.embeds.confirmation.embed.title")
        )
          .setFooter(
            context.translate(
              "misc.confirmation.timeoutText",
              "timeout" to (confirmation.timeout / 1_000).toString()
            )
          )
      )

    if (!result) {
      confirmation.sentMessage!!.editMessageEmbeds(
        context.embedTemplates.normal(
          context.translate("modules.pokemon.commands.release.embeds.cancelled.description"),
          context.translate("modules.pokemon.commands.release.embeds.cancelled.title")
        ).build()
      ).queue()
      return
    }

    val session = module.bot.database.startSession()

    session.use { clientSession ->
      clientSession.startTransaction()

      var pokemonsMess = ""
      var rewardsMess = ""

      val rewardsMap = mutableMapOf<EVItem.EVItems, Int>()

      authorPokemon.map { pokemon ->

        val evAmount = (if (pokemon.level % 2 == 0) pokemon.level else pokemon.level - 1) / 2
        for(i in 0 until evAmount) {
          val randomEV: EVItem.EVItems = EVItem.getRandom()
          rewardsMap.putIfAbsent(randomEV, 0)
          rewardsMap[randomEV] = rewardsMap[randomEV]!!.plus(1)
        }

        module.bot.database.pokemonRepository.releasePokemon(pokemon, clientSession)
        module.bot.database.userRepository.releasePokemon(authorUserData, pokemon, clientSession)

        pokemonsMess += (context.translator.pokemonDisplayName(pokemon) + ", ")
      }

      for ((key, value) in rewardsMap) {
        module.bot.database.userRepository.addInventoryItem(context.author.id, key.id, value, session)

        rewardsMess += "$value ${key.itemName}, "
      }

      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.pokemon.commands.release.embeds.released.description",
            mapOf(
              "pokemon" to pokemonsMess.dropLast(2),
              "rewards" to rewardsMess.dropLast(2)
            )
          ),
          context.translate("modules.pokemon.commands.release.embeds.released.title")
        ).build()
      ).queue()

      clientSession.commitTransactionAndAwait()
    }

    context.bot.database.tradeRepository.endTrade(releaseState)
  }
}