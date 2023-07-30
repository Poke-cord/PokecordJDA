package xyz.pokecord.bot.modules.transfer.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.modules.transfer.TransferModule

object TransferStatusCommand : Command() {
  override val name: String = "status"

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

    val authorPokemon = context.bot.database.pokemonRepository.getPokemonByIds(transferState.pokemon)

    val authorPokemonText =
      TransferModule.getTransferStatePokemonText(context, authorPokemon)


    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.transfer.embeds.status.description",
          mapOf(
          "pokemon" to authorPokemonText.joinToString("\n").ifEmpty { "None" }
          )
        ),
        context.translate("modules.transfer.embeds.status.title")
      ).build()
    ).queue()
    return
  }
}