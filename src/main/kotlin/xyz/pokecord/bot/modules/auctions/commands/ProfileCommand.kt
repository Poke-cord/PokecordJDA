package xyz.pokecord.bot.modules.auctions.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.EmbedTemplates
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.EmbedPaginator
import kotlin.math.ceil

object ProfileCommand: Command() {
  override val name = "Profile"

  suspend fun execute(
    context: ICommandContext,
    @Argument(optional = true) bids: String?,
    @Argument(optional = true) page: Int?,
    @Argument(optional = true) user: User?
  ) {
    if(!context.hasStarted(true)) return

    val targetUser = user ?: context.author
    val targetData = context.bot.database.userRepository.getUser(targetUser)
    if(!context.isStaff() && user != null && targetData.progressPrivate) {
      context.reply(context.embedTemplates.progressPrivate(targetUser).build()).queue()
      return
    }

    val templateEmbedBuilder =
      EmbedBuilder()
        .setTitle(
          context.translate(
            "modules.auctions.commands.profile.embeds.title",
            "user" to targetUser.asTag
          )
        )
        .setColor(EmbedTemplates.Color.GREEN.code)

    val count = context.bot.database.auctionRepository.getAuctionCount(targetUser.id)
    if (count < 1) {
      context.reply(
        templateEmbedBuilder.setDescription(context.translate("modules.auctions.commands.profile.errors.noResults"))
          .setColor(EmbedTemplates.Color.RED.code).build()
      ).queue()
      return
    }

    val pageCount = ceil((count.toDouble() / 10)).toInt()
    val paginator = EmbedPaginator(context, pageCount, { pageIndex ->
      if (pageIndex >= pageCount) {
        return@EmbedPaginator templateEmbedBuilder.setDescription(context.translate("modules.auctions.commands.auctions.errors.noResults"))
          .setColor(EmbedTemplates.Color.RED.code).setFooter("")
      }
      val auctionsList = context.bot.database.auctionRepository.getAuctionList(targetUser.id, skip = pageIndex * 10)
      templateEmbedBuilder.clearFields().setFooter(null)
      templateEmbedBuilder.setDescription(
        AuctionsCommand.formatAuctions(
          context,
          auctionsList,
          bids == "b" || bids == "bid" || bids == "bids"
        )
      )
      templateEmbedBuilder
    }, if (page == null) 0 else page - 1)
    paginator.start()
  }
}