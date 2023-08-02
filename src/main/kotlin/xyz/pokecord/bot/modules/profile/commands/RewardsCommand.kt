package xyz.pokecord.bot.modules.profile.commands

import org.litote.kmongo.coroutine.abortTransactionAndAwait
import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.repositories.PokemonRepository
import xyz.pokecord.bot.core.structures.discord.base.Command

class RewardsCommand : Command() {
  override val name = "rewards"
  override var aliases = arrayOf("rw", "rws", "reward")

  private val catchValues = arrayOf("catch", "pokemon", "c", "p")
  private val allValues = arrayOf("all", "a")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(name = "catch/all", optional = true) action: String?
  ) {
    if (!context.hasStarted(true)) return

    var rewardsClaimed = 0

    if (action == null) {
      val unclaimedCatchRewards = module.bot.database.pokemonRepository.getUnclaimedPokemonCount(context.author.id)
      val catchBreakdown = module.bot.database.pokemonRepository.getSpecificUnclaimedPokemonCount(context.author.id)
      val otherCount = (
          unclaimedCatchRewards-(catchBreakdown.mythicCount.toInt()+catchBreakdown.leggyCount.toInt()+catchBreakdown.ubCount.toInt()+catchBreakdown.psLeggyCount.toInt())
          )

      if (unclaimedCatchRewards < 1) {
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
              "unclaimedCatchRewards" to unclaimedCatchRewards.toString(),
              "mythicCount" to (catchBreakdown.mythicCount.toString()),
              "legendaryCount" to (catchBreakdown.leggyCount.toString()),
              "ubCount" to (catchBreakdown.ubCount.toString()),
              "psLegendaryCount" to (catchBreakdown.psLeggyCount.toString()),
              "otherCount" to (otherCount.toString()),
            )
          ),
          context.translate(
            "modules.profile.commands.rewards.embeds.available.title",
            "totalRewards" to unclaimedCatchRewards.toString()
          )
        ).build()
      ).queue()
    } else {
      val embed = context.embedTemplates.empty()
        .setDescription(context.translate("modules.profile.commands.rewards.embed.description"))

      val lowerCaseAction = action.lowercase()
      val all = allValues.contains(lowerCaseAction)
      val catch = catchValues.contains(lowerCaseAction)

      if (all || catch) {
        val catchRewardsResult = giveCatchRewards(context)
        if (catchRewardsResult == null) {
          // transaction cancelled
          context.reply(
            context.embedTemplates.normal(
              context.translate(
                "misc.embeds.transactionCancelled.description",
                mapOf(
                  "type" to "claim rewards"
                )
              ),
              context.translate("misc.embeds.transactionCancelled.title")
            ).build()
          ).queue()
          return
        }

        val (claimResult, claimedCredits) = catchRewardsResult
        if (!all && claimedCredits < 1) {
          context.reply(
            context.embedTemplates.error(
              context.translate("modules.profile.commands.rewards.errors.noPokemonRewards")
            ).build()
          ).queue()
          return
        }
        rewardsClaimed += claimResult.totalCount
        embed.addField(
          context.translate(
            "modules.profile.commands.rewards.embed.fields.catchRewards.name",
            "amount" to claimResult.totalCount.toString()
          ),
          context.translate(
            "modules.profile.commands.rewards.embed.fields.catchRewards.value",
            mapOf(
              "credits" to context.translator.numberFormat(claimedCredits),
              "user" to context.author.asMention
            )
          ),
          false
        ).setFooter(context.translate("modules.profile.commands.rewards.embed.footer"))
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
  ): Pair<PokemonRepository.CatchRewardClaimResult, Int>? {
    val userData = context.getUserData()
    var claimed = 0
    var claimResult: PokemonRepository.CatchRewardClaimResult
    val session = module.bot.database.startSession()
    session.use {
      it.startTransaction()
      claimResult = module.bot.database.pokemonRepository.claimUnclaimedPokemon(context.author.id, it)
      claimed += claimResult.mythicalCount * 100
      claimed += claimResult.legendaryCount * 85
      claimed += claimResult.ultraBeastCount * 65
      claimed += claimResult.pseudoLegendaryCount * 50
      claimed += claimResult.otherCount * 35
      if (claimed > 0) {
        if (!module.bot.database.userRepository.incCredits(userData, claimed, it)) {
          it.abortTransactionAndAwait()
          return null
        }
      }
      it.commitTransactionAndAwait()
    }
    return Pair(claimResult, claimed)
  }
}
