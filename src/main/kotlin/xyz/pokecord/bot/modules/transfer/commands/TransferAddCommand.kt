package xyz.pokecord.bot.modules.transfer.commands

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.modules.transfer.TransferModule
import xyz.pokecord.bot.utils.Config

object TransferAddCommand : Command() {
  override val name: String = "add"
  override var aliases = arrayOf("a")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument pokemon: IntArray?
  ) {
    if (!context.hasStarted(true)) return

    val transferState = context.getTransferState()
    val userData = context.getUserData()

    if (pokemon == null) {
      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.transfer.embeds.usage.description",
            mapOf(
              "prefix" to context.getPrefix()
            )
          ),
          context.translate("modules.transfer.embeds.usage.title")
        ).build()
      ).queue()
      return
    }

    if (transferState == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.transfer.errors.notInTransfer")
        ).build()
      ).queue()
      return
    }

    val pokemonList = pokemon.toSet().mapNotNull {
      context.bot.database.pokemonRepository.getPokemonByIndex(
        context.author.id,
        it - 1
      )  // Pokémon index starts at 0, but user input starts at 1
    }

    if (transferState.pokemon.size + pokemonList.size > Config.maxTransferSessionPokemon) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.transfer.errors.notInRange")
        ).build()
      ).queue()
      return
    }

    if (pokemonList.isEmpty()) {
      context.reply(context.embedTemplates.error(context.translate("misc.errors.pokemonNotFound")).build())
        .queue()
      return
    }

    pokemonList.forEach { ownedPokemon ->
      when {
        ownedPokemon._id == userData.selected -> {
          context.reply(
            context.embedTemplates.error(context.translate("modules.transfer.errors.selectedPokemon"))
              .build()
          )
            .queue()
          return
        }
        ownedPokemon.favorite -> {
          context.reply(
            context.embedTemplates.error(context.translate("modules.transfer.errors.favoritePokemon"))
              .build()
          )
            .queue()
          return
        }
      }
    }

    val authorPokemonText =
      TransferModule.getTransferStatePokemonText(context, pokemonList)

    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.transfer.embeds.add.description",
          mapOf(
            "pokemon" to authorPokemonText.joinToString("\n").ifEmpty { "None" }
          )
        ),
        context.translate("modules.transfer.embeds.add.title")
      ).build()
    ).queue()

    pokemonList.map { ownedPokemon ->
      val session = context.bot.database.startSession()
      session.use {
        session.startTransaction()
        context.bot.database.transferRepository.addPokemon(transferState, ownedPokemon._id, session)
        session.commitTransactionAndAwait()
      }
    }
  }
}