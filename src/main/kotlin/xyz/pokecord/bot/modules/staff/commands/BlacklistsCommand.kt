package xyz.pokecord.bot.modules.staff.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.modules.staff.StaffCommand
import xyz.pokecord.bot.utils.EmbedPaginator
import kotlin.math.ceil

object BlacklistsCommand : StaffCommand() {
  private const val PAGE_SIZE = 10

  override val name = "Blacklists"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(optional = true) page: Int?
  ) {
    val paginatorIndex = (page ?: 1) - 1

    val blacklistedUserCount = context.bot.database.userRepository.getBlacklistedUserCount()

    if (blacklistedUserCount <= 0) {
      context.reply(
        context.embedTemplates.error(
          "No blacklisted users were found.",
          "Blacklisted Users"
        ).build()
      ).queue()
      return
    }

    EmbedPaginator(context, ceil(blacklistedUserCount / PAGE_SIZE.toFloat()).toInt(), {
      val startingIndex = it * PAGE_SIZE
      val items = context.bot.database.userRepository.getBlacklistedUsers(PAGE_SIZE, startingIndex).toList()
      val userList = items.mapIndexed { index, user ->
        "|`${startingIndex + index + 1}`| **${user.id}** - [${if (user.tag.isNullOrEmpty()) "null" else user.tag}]"
      }.joinToString("\n")
      context.embedTemplates.normal(
        userList,
        "Blacklisted Users"
      )
    }, paginatorIndex).start()
  }
}
