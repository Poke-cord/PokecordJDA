package xyz.pokecord.bot.modules.release.commands

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.modules.release.ReleaseModule
import xyz.pokecord.bot.utils.Config

object ReleaseRemoveCommand : Command() {
  override val name: String = "remove"
  override var aliases = arrayOf("r")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument pokemon: IntArray?
  ) {
    if (!context.hasStarted(true)) return

    val releaseState = context.getReleaseState()
    if (releaseState == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.release.errors.notInRelease")
        ).build()
      ).queue()
      return
    }

    if (pokemon == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("misc.errors.pokemonNotFound")
        ).build()
      ).queue()
      return
    }

    if (pokemon.size > Config.maxReleaseSessionPokemon) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.release.errors.notInRange")
        ).build()
      ).queue()
      return
    }

    val pokemonList = pokemon.toSet().mapNotNull {
      context.bot.database.pokemonRepository.getPokemonByIndex(
        context.author.id,
        it - 1
      ) // Pokemon index starts at 0, but user input starts at 1
    }

    if (pokemonList.isEmpty()) {
      context.reply(
        context.embedTemplates.error(
          context.translate(
            "misc.errors.pokemonNotFound",
            "index" to pokemon.toString()
          )
        ).build()
      ).queue()
    } else {
      val authorPokemonText =
        ReleaseModule.getReleaseStatePokemonText(context, pokemonList)

      val session = context.bot.database.startSession()
      session.use {
        session.startTransaction()
        pokemonList.forEach {
          context.bot.database.releaseRepository.removePokemon(releaseState, it._id, session)
        }
        session.commitTransactionAndAwait()
      }

      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.release.embeds.center.removePokemon.description",
            mapOf(
              "pokemon" to authorPokemonText.joinToString("\n").ifEmpty { "None" }
            )
          ),
          context.translate("modules.release.embeds.center.removePokemon.title")
        ).build()
      ).queue()
    }
  }
}