package xyz.pokecord.bot.modules.staff.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.modules.staff.StaffCommand
import xyz.pokecord.bot.utils.EmbedPaginator
import kotlin.math.ceil

object BlacklistsCommand : StaffCommand() {
  private const val PAGE_SIZE = 10

  override val name = "Blacklists"
  override var aliases = arrayOf("bls")
  
  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(prefixed = true) sus: Boolean?,
    @Argument(optional = true) page: Int?
  ) {
    val paginatorIndex = (page ?: 1) - 1

    val susBlacklistedUsers = context.bot.database.configRepository.getSusBlacklistIds()
    val blacklistedUserCount = if (sus == true) susBlacklistedUsers.size.toLong() else context.bot.database.userRepository.getBlacklistedUserCount()

    if (blacklistedUserCount <= 0) {
      context.reply(
        context.embedTemplates.error(
          "No ${if (sus == true) "sus " else ""}blacklisted users were found. You're dreaming.",
          "${if (sus == true) "Sus " else ""}Blacklisted Users"
        ).build()
      ).queue()
      return
    }

    EmbedPaginator(context, ceil(blacklistedUserCount / PAGE_SIZE.toFloat()).toInt(), {
      val startingIndex = it * PAGE_SIZE
      val items = context.bot.database.userRepository.getBlacklistedUsers(PAGE_SIZE, startingIndex, if (sus == true) susBlacklistedUsers else emptyList()).toList()
      val userList = items.mapIndexed { index, user ->
        "|`${startingIndex + index + 1}`| **${user.id}** - [${if (user.tag.isNullOrEmpty()) "null" else user.tag}]${if (sus != true && susBlacklistedUsers.contains(user.id)) " - [sus]" else ""}"
      }.joinToString("\n")
      context.embedTemplates.normal(
        userList,
        "${if (sus == true) "Sus " else ""}Blacklisted Users"
      )
    }, paginatorIndex).start()
  }
}
