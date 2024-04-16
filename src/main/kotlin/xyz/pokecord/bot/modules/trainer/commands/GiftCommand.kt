package xyz.pokecord.bot.modules.trainer.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.User
import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.modules.trainer.gift.GiftCreditCommand
import xyz.pokecord.bot.modules.trainer.gift.GiftPokemonCommand
import xyz.pokecord.bot.modules.trainer.gift.GiftToggleCommand

object GiftCommand : ParentCommand() {
  override val childCommands = mutableListOf(GiftCreditCommand, GiftPokemonCommand, GiftToggleCommand)
  override val name = "Gift"
  override var aliases = arrayOf("g")

  override var excludeFromHelp = true

  suspend fun receivingGifts(context: ICommandContext, receiver: User): Boolean {
    if (!receiver.giftsEnabled) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.pokemon.commands.gift.errors.giftsDisabled")
        ).build()
      ).queue()
      return false
    }
    return true
  }
}
