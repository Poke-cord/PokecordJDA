package xyz.pokecord.bot.modules.transfer.commands

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.items.EVItem
import xyz.pokecord.bot.modules.transfer.TransferModule
import xyz.pokecord.bot.utils.Confirmation

object TransferConfirmCommand : Command() {
  override val name: String = "confirm"

  @Executor
  suspend fun execute(
    context: ICommandContext
  ) {
    if (!context.hasStarted(true)) return

    val transferState = context.getTransferState()
    if (transferState == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.transfer.errors.notInTransfer")
        ).build()
      ).queue()
      return
    }

    if (transferState.pokemon.isEmpty()) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.transfer.embeds.confirmation.errors.emptyTransfer")
        ).build()
      ).queue()
      return
    }

    val authorUserData = context.bot.database.userRepository.getUser(transferState.userId)
    val authorPokemon = context.bot.database.pokemonRepository.getPokemonByIds(transferState.pokemon)

    val authorPokemonText =
      TransferModule.getTransferStatePokemonText(context, authorPokemon)
    val confirmation = Confirmation(context)
    val result =
      confirmation.result(
        context.embedTemplates.confirmation(
          context.translate(
            "modules.transfer.embeds.confirmation.embeds.pending.description",
            mapOf(
              "pokemon" to authorPokemonText.joinToString("\n").ifEmpty { "None" }
            )
          ),
          context.translate("modules.transfer.embeds.confirmation.embeds.pending.title")
        )
          .setFooter(
            context.translate(
              "misc.confirmation.timeoutFooter",
              "timeout" to (confirmation.timeout / 1_000).toString()
            )
          )
      )

    if (!result) {
      confirmation.sentMessage!!.editMessageEmbeds(
        context.embedTemplates.normal(
          context.translate("modules.transfer.embeds.confirmation.embeds.timeout.description"),
          context.translate("modules.transfer.embeds.confirmation.embeds.timeout.title")
        ).build()
      ).queue()
      return
    }

    val session = module.bot.database.startSession()

    session.use { clientSession ->
      clientSession.startTransaction()

      var rewardsMess = ""

      val rewardsMap = mutableMapOf<EVItem.EVItems, Int>()

      authorPokemon.map { pokemon ->

        val evAmount = (if (pokemon.level % 2 == 0) pokemon.level else pokemon.level - 1) / 2
        for (i in 0 until evAmount) {
          val randomEV: EVItem.EVItems = EVItem.getRandom()
          rewardsMap.putIfAbsent(randomEV, 0)
          rewardsMap[randomEV] = rewardsMap[randomEV]!!.plus(1)
        }

        module.bot.database.pokemonRepository.transferPokemon(pokemon, clientSession)
        module.bot.database.userRepository.transferPokemon(authorUserData, pokemon, clientSession)
      }

      for ((key, value) in rewardsMap) {
        module.bot.database.userRepository.addInventoryItem(context.author.id, key.id, value, session)

        rewardsMess += "$value ${key.itemName}, "
      }

      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.transfer.embeds.transferred.description",
            mapOf(
              "pokemon" to authorPokemonText.joinToString("\n").ifEmpty { "None" },
              "rewards" to rewardsMess.dropLast(2).ifEmpty { "Nothing. How sad." }
            )
          ),
          context.translate("modules.transfer.embeds.transferred.title")
        ).build()
      ).queue()

      clientSession.commitTransactionAndAwait()
    }

    context.bot.database.transferRepository.endTransfer(transferState)
  }
}