package xyz.pokecord.bot.modules.general.commands

import xyz.pokecord.bot.core.structures.discord.Command
import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext
import xyz.pokecord.bot.utils.VoteUtils
import kotlin.math.min

class VoteCommand : Command() {
  override val name = "Vote"

  @Executor
  suspend fun execute(
    context: MessageReceivedContext
  ) {
    val season = VoteUtils.getCurrentSeason()

    val voteRewards = module.bot.database.rewardRepository.getVoteRewards(context.author.id)
    val currentSeasonVotes = voteRewards.filter { it.season == season }
    val currentVoteStreak = min(currentSeasonVotes.size, 30)
    val unclaimedVotes = currentSeasonVotes.count { !it.claimed }

    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.general.commands.vote.embed.description",
          mapOf(
            "voteStreak" to currentVoteStreak.toString(),
            "unclaimedVotes" to unclaimedVotes.toString(),
            "currentSeasonEndTime" to VoteUtils.getSeasonEndTime(),
            "voteLink" to "https://top.gg/bot/${context.jda.selfUser.id}/vote",
            "prefix" to context.getPrefix()
          )
        ),
        context.translate("modules.general.commands.vote.embed.title", "season" to season.toString())
      )
        .setFooter(context.translate("modules.general.commands.vote.embed.footer"))
        .setImage("https://votemap.s3.wasabisys.com/votemap/$currentVoteStreak.png")
        .build()
    ).queue()
  }
}
