package xyz.pokecord.bot.modules.staff.commands

import net.dv8tion.jda.api.entities.User
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.modules.staff.StaffCommand

class BlacklistCommand : StaffCommand() {
  override val name = "Blacklist"
  override var aliases = arrayOf("bl")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(prefixed = true) sus: Boolean?,
    @Argument user: User?
  ) {
    if (user == null) {
      context.reply(context.embedTemplates.error("gib user pls").build()).queue()
      return
    }
    val userData = context.bot.database.userRepository.getUser(user)
    context.bot.database.userRepository.setBlacklisted(userData, !userData.blacklisted)

    var wasSus = false
    if (sus == true || !userData.blacklisted) {
      if (userData.blacklisted) context.bot.database.configRepository.addSusBlacklist(user.id)
      else {
        wasSus = context.bot.database.configRepository.getSusBlacklistIds().contains(user.id)
        context.bot.database.configRepository.removeSusBlacklist(user.id)
      }
    }

    context.reply(
      context.embedTemplates.normal("${user.asMention} has been ${if (userData.blacklisted) "blacklisted" else "whitelisted"}${if ((userData.blacklisted && sus == true) || wasSus) " [sus]" else ""}.")
        .build()
    ).queue()
  }
}
