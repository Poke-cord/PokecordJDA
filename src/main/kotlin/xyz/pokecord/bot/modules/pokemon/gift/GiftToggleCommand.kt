package xyz.pokecord.bot.modules.pokemon.gift

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command


object GiftToggleCommand : Command() {
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
