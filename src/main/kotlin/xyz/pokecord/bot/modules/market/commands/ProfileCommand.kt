package xyz.pokecord.bot.modules.market.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import org.litote.kmongo.eq
import org.litote.kmongo.match
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.Listing
import xyz.pokecord.bot.core.structures.discord.EmbedTemplates
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.EmbedPaginator
import kotlin.math.ceil

object ProfileCommand : Command() {
  override val name = "Profile"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(optional = true) page: Int?,
    @Argument(optional = true) user: User?
  ) {
    if (!context.hasStarted(true)) return

    val targetUser = user ?: context.author
    val targetData = context.bot.database.userRepository.getUser(targetUser)
    if (!context.isStaff() && user != null && targetData.progressPrivate) {
      context.reply(context.embedTemplates.progressPrivate(targetUser).build()).queue()
      return
    }

    val templateEmbedBuilder =
      EmbedBuilder()
        .setTitle(
          context.translate(
            "modules.market.commands.profile.embeds.title",
            "userTag" to targetUser.asTag
          )
        )
        .setColor(EmbedTemplates.Color.GREEN.code)

    val aggregation = listOf(match(Listing::sold eq false, Listing::unlisted eq false))

    val count =
      context.bot.database.marketRepository.getListingCount(targetUser.id, aggregation = aggregation.toMutableList())
    if (count < 1) {
      context.reply(
        templateEmbedBuilder.setDescription(context.translate("modules.market.commands.profile.errors.noResults"))
          .setColor(EmbedTemplates.Color.RED.code).build()
      ).queue()
      return
    }

    val pageCount = ceil((count.toDouble() / 10)).toInt()
    val paginator = EmbedPaginator(context, pageCount, { pageIndex ->
      if (pageIndex >= pageCount) {
        return@EmbedPaginator templateEmbedBuilder.setDescription(context.translate("modules.market.commands.market.errors.noResults"))
          .setColor(EmbedTemplates.Color.RED.code).setFooter("")
      }
      val listings = context.bot.database.marketRepository.getListings(
        targetUser.id,
        skip = pageIndex * 10,
        aggregation = aggregation.toMutableList()
      )
      templateEmbedBuilder.clearFields().setFooter(null)
      templateEmbedBuilder.setDescription(
        MarketCommand.formatListings(context, listings)
      )
      templateEmbedBuilder
    }, if (page == null) 0 else page - 1)
    paginator.start()
  }
}