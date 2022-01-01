package xyz.pokecord.bot.modules.general.commands.set

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object SetPrivateCommand : Command() {
  override val name = "Private"

  @Executor
  suspend fun execute(context: ICommandContext) {
    val userData = context.getUserData()
    module.bot.database.userRepository.togglePrivate(userData)
    context.reply(
      context.embedTemplates.normal(
        context.translate(if (userData.progressPrivate) "modules.general.commands.set.private.enabled" else "modules.general.commands.set.private.disabled"),
        context.translate("modules.general.commands.set.private.title")
      ).build()
    ).queue()
  }
}