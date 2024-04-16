package xyz.pokecord.bot.modules.auction.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object RemindCommand : Command() {
  override val name = "Remind"
  override var aliases = arrayOf("rm", "rmd", "reminder")

  @Executor
  suspend fun execute(context: ICommandContext) {
    if (!context.hasStarted(true)) return

    val userData = context.getUserData()
    if(userData.bidNotifications) {
      context.reply(
        context.embedTemplates.normal(
          context.translate("modules.auctions.commands.notify.toggledOff.description"),
          context.translate("modules.auctions.commands.notify.toggledOff.title")
        ).build()
      ).queue()
    } else {
      context.reply(
        context.embedTemplates.normal(
          context.translate("modules.auctions.commands.notify.toggledOn.description"),
          context.translate("modules.auctions.commands.notify.toggledOn.title")
        ).build()
      ).queue()
    }

    context.bot.database.userRepository.toggleBidNotifications(userData)
  }
}