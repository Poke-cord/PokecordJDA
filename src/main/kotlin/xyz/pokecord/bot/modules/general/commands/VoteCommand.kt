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
  suspend fun execute(context: ICommandContext) {
    val lastVoteAt = context.getUserData().lastVoteAt
    val nextVoteTime =
      if (lastVoteAt == null || lastVoteAt + TimeUnit.HOURS.toMillis(12) < System.currentTimeMillis()) context.translate(
        "modules.general.commands.vote.now"
      ) else TimeFormat.RELATIVE.format(lastVoteAt + TimeUnit.HOURS.toMillis(12))
    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.general.commands.vote.embed.description",
          mapOf(
            "currentSeasonEndTime" to context.translator.dateFormat(
              Date.from(VoteUtils.getSeasonEndTime().atStartOfDay().atZone(ZoneOffset.UTC).toInstant())
            ),
            "voteLink" to "https://top.gg/bot/${context.jda.selfUser.id}/vote",
            "nextVoteTime" to nextVoteTime
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
