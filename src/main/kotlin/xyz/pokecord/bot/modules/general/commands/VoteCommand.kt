package xyz.pokecord.bot.modules.general.commands

import net.dv8tion.jda.api.utils.TimeFormat
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.VoteUtils
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.TimeUnit

class VoteCommand : Command() {
  override val name = "Vote"

  @Executor
  suspend fun execute(
    context: ICommandContext
  ) {
    val lastVoteAt = context.getUserData().lastVoteAt
    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.general.commands.vote.embed.description",
          mapOf(
            "currentSeasonEndTime" to context.translator.dateFormat(
              Date.from(VoteUtils.getSeasonEndTime().atStartOfDay().atZone(ZoneOffset.UTC).toInstant())
            ),
            "voteLink" to "https://top.gg/bot/${context.jda.selfUser.id}/vote",
            "nextVoteTime" to (lastVoteAt?.let {
              TimeFormat.RELATIVE.format(it + TimeUnit.HOURS.toMillis(12))
            } ?: context.translate("modules.general.commands.vote.now"))
          )
        ),
        context.translate(
          "modules.general.commands.vote.embed.title",
          "season" to VoteUtils.getCurrentSeason().toString()
        )
      )
        .setFooter(context.translate("modules.general.commands.vote.embed.footer"))
        .setImage("https://votemap.s3.wasabisys.com/votemap/${VoteUtils.getSeasonDay()}.png")
        .build()
    ).queue()
  }
}
