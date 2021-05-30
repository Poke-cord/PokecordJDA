package xyz.pokecord.bot.modules.profile.commands

import net.dv8tion.jda.api.entities.User
import xyz.pokecord.bot.core.structures.discord.Command
import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext

class BalanceCommand : Command() {
  override val name = "Balance"

  override var aliases = arrayOf("bal", "credits")

  @Executor
  suspend fun execute(
    context: MessageReceivedContext,
    @Argument(optional = true) user: User?
  ) {
    if (!context.hasStarted(true)) return
    val targetUser = user ?: context.author
    val checkingSelf = user == null

    val userData = if (checkingSelf) context.getUserData() else module.bot.database.userRepository.getUser(targetUser)
    // TODO: moderator check
    if (userData.progressPrivate && !checkingSelf) {
      context.reply(context.embedTemplates.progressPrivate(targetUser).build()).queue()
      return
    }

    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.profile.commands.balance.description",
          mapOf(
            "credits" to context.translator.numberFormat(userData.credits),
            "gems" to context.translator.numberFormat(userData.gems)
          )
        ),
        context.translate("modules.profile.commands.balance.title", "user" to targetUser.asTag)
      ).build()
    ).queue()
  }
}
