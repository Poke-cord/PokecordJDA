package xyz.pokecord.bot.modules.staff.commands

import net.dv8tion.jda.api.entities.User
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.modules.staff.StaffCommand

class BlacklistCommand : StaffCommand() {
  override val name = "Blacklist"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument user: User?
  ) {
    if (user == null) {
      context.reply(context.embedTemplates.error("gib user pls").build()).queue()
      return
    }
    val userData = context.bot.database.userRepository.getUser(user)
    context.bot.database.userRepository.setBlacklisted(userData, !userData.blacklisted)

    context.reply(
      context.embedTemplates.normal("${user.asMention} has been ${if (userData.blacklisted) "blacklisted" else "un-blacklisted"}")
        .build()
    ).queue()
  }
}
