package xyz.pokecord.bot.modules.profile.commands.profile

import net.dv8tion.jda.api.entities.User
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object ProfileStatisticsCommand : Command() {
  override val name = "Statistics"
  override var aliases = arrayOf("s", "i", "stats", "info")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(optional = true) user: User?
  ) {
    val checkingSelf = user == null
    val targetUser = user ?: context.author

    val userData = if (checkingSelf) context.getUserData() else module.bot.database.userRepository.getUser(targetUser)
    if (!context.isStaff() && userData.progressPrivate && !checkingSelf) {
      context.reply(context.embedTemplates.progressPrivate(targetUser).build()).queue()
      return
    }

    context.reply(
      context.embedTemplates.normal(
        context.translate("modules.profile.commands.profile.embed.info.description",
          mapOf(
            //"pokemonCount" to userData.pokemonCount.toString(),
            "rate" to userData.shinyRate.toString(),
          )
        ),
        context.translate("modules.profile.commands.profile.embed.info.title",
          "user" to context.author.asTag
        )
      ).setFooter(context.translate("modules.profile.commands.profile.embed.info.footer"))
        .build()
    ).queue()
  }
}
