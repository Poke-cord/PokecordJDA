package xyz.pokecord.bot.modules.profile.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object RemindCommand : Command() {
  override val name = "Remind"
  override var aliases = arrayOf("rm", "rms", "rmd", "reminder", "reminders")

  private val voteAliases = arrayOf("vote", "v", "next vote reminder")
  private val auctionAliases = arrayOf("au", "ah", "auction", "auction activity")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(name = "reminder name", optional = true) reminder: String?
  ) {
    val enabled = "`ENABLED`"
    val disabled = "`DISABLED`"
    val userData = context.getUserData()

    if (reminder == null) {
      context.reply(
        context.embedTemplates.normal(
          context.translate("modules.profile.commands.remind.embed.description.base",
            mapOf(
              "vote" to if (userData.voteReminder) enabled else disabled,
              "auction" to if (userData.bidNotifications) enabled else disabled
            )
          ),
          context.translate("modules.profile.commands.remind.embed.title.base",
            "user" to context.author.asTag
          )
        ).setFooter(context.translate("modules.profile.commands.remind.embed.footer")).build()
      ).queue()
      return
    }
    else {
      val userInput = reminder.lowercase()
      val vote = voteAliases.contains(userInput)
      val auction = auctionAliases.contains(userInput)

      if (vote) {
        context.reply(
          context.embedTemplates.normal(
            context.translate(
              if (userData.voteReminder) "modules.profile.commands.remind.embed.description.disabled"
              else "modules.profile.commands.remind.embed.description.enabled"
            ),
            context.translate(
              "modules.profile.commands.remind.embed.title.toggle",
              "reminderType" to "Vote"
            )
          ).build()
        ).queue()
        module.bot.database.userRepository.toggleVoteReminder(userData)
        return
      }

      if (auction) {
        context.reply(
          context.embedTemplates.normal(
            context.translate(
              if (userData.voteReminder) "modules.profile.commands.remind.embed.description.disabled"
              else "modules.profile.commands.remind.embed.description.enabled"
            ),
            context.translate(
              "modules.profile.commands.remind.embed.title.toggle",
              "reminderType" to "Auction"
            )
          ).build()
        ).queue()
        context.bot.database.userRepository.toggleBidNotifications(userData)
        return
      }
    }
  }
}