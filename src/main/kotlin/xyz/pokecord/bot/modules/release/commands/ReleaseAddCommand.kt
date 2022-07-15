package xyz.pokecord.bot.modules.release.commands

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.modules.release.ReleaseModule
import xyz.pokecord.bot.utils.Config

object ReleaseAddCommand : Command() {
  override val name: String = "add"
  override var aliases = arrayOf("a")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument pokemon: IntArray?
  ) {
    if (!context.hasStarted(true)) return

    val releaseState = context.getReleaseState()
    val userData = context.getUserData()

    if (pokemon == null) {
      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.release.embeds.center.embed.description",
            mapOf(
              "prefix" to context.getPrefix()
            )
          ),
          context.translate("modules.release.embeds.center.embed.title")
        ).build()
      ).queue()
      return
    }

    if (releaseState == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.release.errors.notInRelease")
        ).build()
      ).queue()
      return
    }

    val pokemonList = pokemon.toSet().mapNotNull {
      context.bot.database.pokemonRepository.getPokemonByIndex(
        context.author.id,
        it - 1
      )  // Pokemon index starts at 0, but user input starts at 1
    }

    if (releaseState.pokemon.size + pokemonList.size > Config.maxReleaseSessionPokemon) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.release.errors.notInRange")
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
            context.embedTemplates.error(context.translate("modules.release.errors.selectedPokemon"))
              .build()
          )
            .queue()
          return
        }
        ownedPokemon.favorite -> {
          context.reply(
            context.embedTemplates.error(context.translate("modules.release.errors.favoritePokemon"))
              .build()
          )
            .queue()
          return
        }
      }
    }

    val authorPokemonText =
      ReleaseModule.getReleaseStatePokemonText(context, pokemonList)

    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.release.embeds.confirmation.embedNoConfirm.description",
          mapOf(
            "pokemon" to authorPokemonText.joinToString("\n").ifEmpty { "None" }
          )
        ),
        context.translate("modules.release.embeds.confirmation.embedNoConfirm.title")
      ).build()
    ).queue()

    pokemonList.map { ownedPokemon ->
      val session = context.bot.database.startSession()
      session.use {
        session.startTransaction()
        context.bot.database.releaseRepository.addPokemon(releaseState, ownedPokemon._id, session)
        session.commitTransactionAndAwait()
      }
    }
  }
}