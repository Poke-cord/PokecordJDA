package xyz.pokecord.bot.modules.pokemon.gift

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.modules.staff.StaffCommand

object GiftToggleCommand : StaffCommand() {
  override val name = "Toggle"

  override var aliases = arrayOf("t")

  @Executor
  suspend fun execute(context: ICommandContext) {
    val userData = context.getUserData()
    module.bot.database.userRepository.toggleGifts(userData)
    context.reply(
      context.embedTemplates.normal(
        context.translate(if (userData.giftsEnabled) "modules.pokemon.commands.gift.toggle.enabled" else "modules.pokemon.commands.gift.toggle.disabled"),
        context.translate("modules.pokemon.commands.gift.toggle.title")
      ).build()
    ).queue()
  }
}
