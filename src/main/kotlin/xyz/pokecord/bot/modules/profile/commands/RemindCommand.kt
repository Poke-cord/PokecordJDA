package xyz.pokecord.bot.modules.profile.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object RemindCommand : Command() {
  override val name = "Remind"
  override var aliases = arrayOf("rm", "rms", "rmd", "reminder", "reminders")

  private val voteAliases = arrayOf("vote", "v", "next vote reminder")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(name = "reminder name", optional = true) reminder: String?
  ) {
    val userData = context.getUserData()
    val enabled = "`ENABLED`"
    val disabled = "`DISABLED`"

    if (reminder == null) {
      context.reply(
        context.embedTemplates.normal(
          context.translate("modules.profile.commands.remind.embed.description.base",
            mapOf(
              "vote" to if (userData.voteReminder) enabled else disabled
            )
          ),
          context.translate("modules.profile.commands.remind.embed.title.base",
            "user" to context.author.asTag
          )
        ).setFooter(context.translate("modules.profile.commands.remind.embed.footer")).build()
      ).queue()
    }
    else {
      val lowerCaseReminder = reminder.lowercase()
      val vote = voteAliases.contains(lowerCaseReminder)

      if(vote) {
        module.bot.database.userRepository.toggleVoteReminder(userData)
        context.reply(
          context.embedTemplates.normal(
            context.translate(
              if (userData.voteReminder) "modules.profile.commands.remind.embed.description.enabled"
              else "modules.profile.commands.remind.embed.description.disabled"
            ),
            context.translate("modules.profile.commands.remind.embed.title.toggle",
              "reminderType" to "Vote")
          ).build()
        ).queue()
        return
      }
    }
  }
}