package xyz.pokecord.bot.modules.profile.commands

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.Config
import xyz.pokecord.bot.utils.VoteUtils
import kotlin.random.Random

class RewardsCommand : Command() {
  override val name = "rewards"

  private val voteValues = arrayOf("vote", "voting", "v")
  private val catchValues = arrayOf("catch", "pokemon", "c", "p")
  private val allValues = arrayOf("all", "a")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(name = "vote/catch/all", optional = true) action: String?
  ) {
    if (!context.hasStarted(true)) return

    var rewardsClaimed = 0

    if (action == null) {
      val unclaimedCatchRewards = module.bot.database.pokemonRepository.getUnclaimedPokemonCount(context.author.id)
      val season = VoteUtils.getCurrentSeason()
      val voteRewards = module.bot.database.rewardRepository.getVoteRewards(context.author.id)
      val currentSeasonVotes = voteRewards.filter { it.season == season }
      val unclaimedVoteRewards = currentSeasonVotes.count { !it.claimed }
      val totalRewards = unclaimedCatchRewards + unclaimedVoteRewards

      if (totalRewards < 1) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.profile.commands.rewards.errors.noRewards")
          ).build()
        ).queue()
        return
      }

      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.profile.commands.rewards.embeds.available.description",
            mapOf(
              "unclaimedVoteRewards" to unclaimedVoteRewards.toString(),
              "unclaimedCatchRewards" to unclaimedCatchRewards.toString(),
              "prefix" to context.getPrefix()
            )
          ),
          context.translate(
            "modules.profile.commands.rewards.embeds.available.title",
            "totalRewards" to totalRewards.toString()
          )
        ).build()
      ).queue()
    } else {
      val embed = context.embedTemplates.empty()
        .setDescription(context.translate("modules.profile.commands.rewards.embed.description"))

      val lowerCaseAction = action.toLowerCase()
      val all = allValues.contains(lowerCaseAction)
      val catch = catchValues.contains(lowerCaseAction)
      val vote = voteValues.contains(lowerCaseAction)

      if (all || catch) {
        val claimedCredits = giveCatchRewards(context)
        if (!all && claimedCredits < 1) {
          context.reply(
            context.embedTemplates.error(
              context.translate("modules.profile.commands.rewards.errors.noPokemonRewards")
            ).build()
          ).queue()
          return
        }
        rewardsClaimed += (claimedCredits / 50)
        embed.addField(
          context.translate(
            "modules.profile.commands.rewards.embed.fields.catchRewards.name",
            "amount" to (claimedCredits / 50).toString()
          ),
          context.translate(
            "modules.profile.commands.rewards.embed.fields.catchRewards.value",
            mapOf(
              "credits" to context.translator.numberFormat(claimedCredits)
            )
          ),
          false
        )
      }
      if (all || vote) {
        val voteRewardResult = giveVoteRewards(context)
        if (!all && voteRewardResult.rewardCount < 1) {
          context.reply(
            context.embedTemplates.error(
              context.translate("modules.profile.commands.rewards.errors.noVoteRewards")
            ).build()
          ).queue()
          return
        }
        rewardsClaimed += voteRewardResult.rewardCount
        embed.addField(
          context.translate(
            "modules.profile.commands.rewards.embed.fields.voteRewards.name",
            "amount" to voteRewardResult.rewardCount.toString()
          ),
          context.translate(
            "modules.profile.commands.rewards.embed.fields.voteRewards.value",
            mapOf(
              "credits" to context.translator.numberFormat(voteRewardResult.credits),
              "gems" to context.translator.numberFormat(voteRewardResult.gems),
              "xp" to context.translator.numberFormat(voteRewardResult.xp)
            )
          ),
          false
        )
      }

      if (rewardsClaimed < 1) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.profile.commands.rewards.errors.noRewards")
          ).build()
        ).queue()
      } else {
        context.reply(
          embed.setTitle(
            context.translate(
              "modules.profile.commands.rewards.embed.title",
              "amount" to rewardsClaimed.toString()
            )
          ).build()
        ).queue()
      }
    }
  }

  private suspend fun giveCatchRewards(
    context: ICommandContext
  ): Int {
    val userData = context.getUserData()
    var claimed = 0
    val session = module.bot.database.startSession()
    session.use {
      it.startTransaction()
      val claimResult = module.bot.database.pokemonRepository.claimUnclaimedPokemon(context.author.id, it)
      claimed += claimResult.mythicalCount * 100
      claimed += claimResult.legendaryCount * 85
      claimed += claimResult.ultraBeastCount * 65
      claimed += claimResult.pseudoLegendaryCount * 50
      claimed += claimResult.otherCount * 35
      if (claimed > 0) {
        module.bot.database.userRepository.incCredits(userData, claimed, it)
      }
      it.commitTransactionAndAwait()
    }
    return claimed
  }

  private suspend fun giveVoteRewards(
    context: ICommandContext,
    season: Int = VoteUtils.getCurrentSeason()
  ): VoteRewardResult {
    val voteRewards = module.bot.database.rewardRepository.getVoteRewards(context.author.id)
    val currentSeasonVotes = voteRewards.filter { it.season == season }
    val unclaimedVoteRewards = currentSeasonVotes.filter { !it.claimed }
    val userData = context.getUserData()
    val voteLevel = currentSeasonVotes.size - 1

    val rewards = unclaimedVoteRewards.mapNotNull {
      val tierRewards = Config.VoteRewards.values().find { it.minTier <= voteLevel && it.maxTier >= voteLevel }
        ?: return@mapNotNull null
      val gainedCredits = Random.nextInt(
        tierRewards.minCredits, tierRewards.maxCredits + 1
      )
      val gainedGems = Random.nextInt(
        tierRewards.minGems, tierRewards.maxGems + 1
      )
      val gainedXp = Random.nextInt(
        tierRewards.minXp, tierRewards.maxXp + 1
      )

      Triple(gainedCredits, gainedGems, gainedXp)
    }

    val totalCredits = rewards.fold(0) { acc, triple -> acc + triple.first }
    val totalGems = rewards.fold(0) { acc, triple -> acc + triple.second }
    val totalXp = rewards.fold(0) { acc, triple -> acc + triple.third }

    val selectedPokemon = module.bot.database.pokemonRepository.getPokemonById(userData.selected!!)!!
    val session = module.bot.database.startSession()
    session.use {
      it.startTransaction()
      module.bot.database.userRepository.incCredits(userData, totalCredits, it)
      module.bot.database.userRepository.incGems(userData, totalGems, it)
      module.bot.database.pokemonRepository.giveXp(selectedPokemon, totalXp, it)
      module.bot.database.rewardRepository.deleteVoteRewards(unclaimedVoteRewards.map { reward -> reward._id }, it)
      it.commitTransactionAndAwait()
    }
    return VoteRewardResult(rewards.size, totalCredits, totalGems, totalXp)
  }

  data class VoteRewardResult(
    val rewardCount: Int,
    val credits: Int,
    val gems: Int,
    val xp: Int
  )
}
