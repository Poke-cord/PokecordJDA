package xyz.pokecord.bot.modules.general.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.VoteUtils
import java.time.ZoneOffset
import java.util.*

class VoteCommand : Command() {
  override val name = "Vote"

  @Executor
  suspend fun execute(
    context: ICommandContext
  ) {
    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.general.commands.vote.embed.description",
          mapOf(
            "currentSeasonEndTime" to context.translator.dateFormat(
              Date.from(VoteUtils.getSeasonEndTime().atStartOfDay().atZone(ZoneOffset.UTC).toInstant())
            ),
            "voteLink" to "https://top.gg/bot/${context.jda.selfUser.id}/vote",
            "prefix" to context.getPrefix()
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
