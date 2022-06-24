package xyz.pokecord.bot.modules.release.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.modules.release.ReleaseModule

object ReleaseStatusCommand : Command() {
  override val name: String = "status"

  @Executor
  suspend fun execute(
    context: ICommandContext
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

    val authorPokemon = context.bot.database.pokemonRepository.getPokemonByIds(releaseState.pokemon)

    val authorPokemonText =
      ReleaseModule.getReleaseStatePokemonText(context, authorPokemon, authorPokemon.map { it.id }, false)


    context.reply(
      context.embedTemplates.normal(
        authorPokemonText.joinToString("\n").ifEmpty { "None" },
        "Release status"
      ).build()
    ).queue()
    return
  }
}