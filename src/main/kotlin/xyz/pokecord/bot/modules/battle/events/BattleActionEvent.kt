package xyz.pokecord.bot.modules.battle.events

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.utils.TimeFormat
import org.bson.types.ObjectId
import org.litote.kmongo.id.toId
import xyz.pokecord.bot.core.managers.database.models.Battle
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.structures.discord.EmbedTemplates
import xyz.pokecord.bot.core.structures.discord.base.Event
import xyz.pokecord.bot.core.structures.pokemon.MoveData
import xyz.pokecord.bot.modules.battle.BattleModule
import java.time.Instant
import kotlin.random.Random

object BattleActionEvent : Event() {
  override val name = "BattleAction"

  val embedTemplates = EmbedTemplates()

  @Handler
  suspend fun onButtonClick(event: ButtonClickEvent) {
    try {
      val button = BattleModule.Buttons.fromComponentId(event.componentId)

      if (button !is BattleModule.Buttons.BattleAction) return

      val battleId = button.battleId

      val battle = module.bot.database.battleRepository.getBattle(ObjectId(battleId).toId()) ?: return
      if (!battle.hasTrainer(event.user.id)) return
      val userData = module.bot.database.userRepository.getUser(event.user)
      if (userData.blacklisted) {
        module.bot.database.battleRepository.endBattle(battle)
        return
      }

      when (button) {
        is BattleModule.Buttons.BattleAction.UseMove -> {
          event.deferReply(true).queue()
          val interactionTrainer =
            if (battle.initiator.id == event.interaction.user.id) battle.initiator else if (battle.partner.id == event.interaction.user.id) battle.partner else null

          if (interactionTrainer == null) {
            event.hook.sendMessageEmbeds(
              embedTemplates.error(
                embedTemplates.translate("modules.battle.events.action.useMove.errors.wrongBattle")
              ).build()
            ).queue()
            return
          }
          val self = if (interactionTrainer == battle.initiator) battle.initiator else battle.partner
          val partner = if (interactionTrainer == battle.initiator) battle.partner else battle.initiator
          val partnerUser = event.jda.retrieveUserById(partner.id).await()

          val partnerData = module.bot.database.userRepository.getUser(partner.id)

          val pokemon = module.bot.database.pokemonRepository.getPokemonById(userData.selected!!)!!
          val partnerPokemon = module.bot.database.pokemonRepository.getPokemonById(partnerData.selected!!)!!

          event.hook.sendMessageEmbeds(
              embedTemplates.normal(
                embedTemplates.translate(
                  "modules.battle.events.action.useMove.description",
                  mapOf(
                    "pokemonName" to pokemon.displayName,
                    "partnerPokemonName" to partnerPokemon.displayName,
                    "currentInitiatorHP" to battle.initiator.pokemonStats.hp.toString(),
                    "currentPartnerHP" to battle.partner.pokemonStats.hp.toString(),
                    "pokemonHP" to pokemon.stats.hp.toString(),
                    "partnerPokemonHP" to partnerPokemon.stats.hp.toString(),
                  )
                ),
                embedTemplates.translate(
                  "modules.battle.events.action.useMove.title",
                  mapOf(
                    "initiatorName" to event.interaction.user.name,
                    "partnerName" to partnerUser.name
                  )
                ),
              )
              .setImage("attachment://battle.png")
              .setTimestamp(Instant.ofEpochMilli(battle.startedAtMillis))
              .build()
          ).addActionRow(pokemon.moves.toSet().mapNotNull {
            if (it == 0) return@mapNotNull null
            Button.primary(
              BattleModule.Buttons.BattleAction.ChooseMove(battleId, it.toString()).toString(),
              MoveData.getById(it)!!.name
            )
          }).queue()
        }
        is BattleModule.Buttons.BattleAction.ChooseMove -> {
          if (battle.endedAtMillis != null) {
            event.replyEmbeds(
              embedTemplates.error(
                embedTemplates.translate("modules.battle.events.action.choseMove.errors.battleEnded")
              ).build()
            ).setEphemeral(true).queue()
            return
          }
          val moveId = button.moveId.toIntOrNull() ?: return
          val moveData = MoveData.getById(moveId) ?: return

          val (self, partner) = battle.getTrainers(event.user.id)
          if (self.pendingMove != null) {
            event.replyEmbeds(
              embedTemplates.error(
                embedTemplates.translate("modules.battle.events.action.choseMove.errors.movePending")
              ).build()
            ).setEphemeral(true).queue()
            return
          }

          event.deferReply().queue()
          module.bot.database.battleRepository.chooseMove(battle, event.user.id, moveId)
          if (battle.shouldUseMove) {
            val partnerUser = try {
              event.jda.retrieveUserById(partner.id).await()
            } catch (e: Throwable) {
              null
            } ?: return
            
            val partnerData = module.bot.database.userRepository.getUser(partner.id)

            val selfPokemon = module.bot.database.pokemonRepository.getPokemonById(userData.selected!!)!!
            val partnerPokemon = module.bot.database.pokemonRepository.getPokemonById(partnerData.selected!!)!!

            val partnerMoveData = MoveData.getById(partner.pendingMove!!)!!

            val useSelfMove = suspend {
              module.bot.database.battleRepository.useMove(
                battle, self.id, selfPokemon, partnerPokemon, moveData
              )
            }
            val usePartnerMove = suspend {
              module.bot.database.battleRepository.useMove(
                battle, partner.id, partnerPokemon, selfPokemon, partnerMoveData
              )
            }

            val selfMoveFirst: Boolean
            var winner: Battle.Trainer?
            val (moveResult, partnerMoveResult) = when {
              moveData.priority > partnerMoveData.priority -> {
                selfMoveFirst = true
                val firstMove = useSelfMove()
                winner = battle.winner
                val secondMove = usePartnerMove()
                if (winner == null) winner = battle.winner
                Pair(firstMove, secondMove)
              }
              partnerMoveData.priority > moveData.priority -> {
                selfMoveFirst = false
                val firstMove = usePartnerMove()
                winner = battle.winner
                val secondMove = useSelfMove()
                if (winner == null) winner = battle.winner
                Pair(secondMove, firstMove)
              }
              self.pokemonStats.speed > partner.pokemonStats.speed -> {
                selfMoveFirst = true
                val firstMove = useSelfMove()
                winner = battle.winner
                val secondMove = usePartnerMove()
                if (winner == null) winner = battle.winner
                Pair(firstMove, secondMove)
              }
              partner.pokemonStats.speed > self.pokemonStats.speed -> {
                selfMoveFirst = false
                val firstMove = usePartnerMove()
                winner = battle.winner
                val secondMove = useSelfMove()
                if (winner == null) winner = battle.winner
                Pair(secondMove, firstMove)
              }
              else -> {
                selfMoveFirst = Random.nextBoolean()
                if (selfMoveFirst) {
                  val firstMove = useSelfMove()
                  winner = battle.winner
                  val secondMove = usePartnerMove()
                  if (winner == null) winner = battle.winner
                  Pair(firstMove, secondMove)
                } else {
                  val firstMove = usePartnerMove()
                  winner = battle.winner
                  val secondMove = useSelfMove()
                  if (winner == null) winner = battle.winner
                  Pair(secondMove, firstMove)
                }
              }
            }
            val loser = winner?.let { if (it.id == self.id) partner else self }
            var gainedXp: Int? = null
            var gainedCredits: Int? = null
            if (winner != null && loser != null) {
              val winnerPokemon = if (self.id == winner.id) selfPokemon else partnerPokemon
              val loserPokemon = if (self.id == loser.id) selfPokemon else partnerPokemon
              gainedXp = Battle.gainedXp(
                winner.id,
                winnerPokemon.data,
                winnerPokemon.trainerId ?: winnerPokemon.ownerId,
                winnerPokemon.level,
                loserPokemon.level
              )
              if (gainedXp > 0) {
                module.bot.database.pokemonRepository.levelUpAndEvolveIfPossible(winnerPokemon, gainedXp = gainedXp)
              }

              if(battle.wager !== null)
                gainedCredits = Battle.gainedCredits(battle.wager)
              if(gainedCredits !== null)
                module.bot.database.userRepository.incCredits(
                  (if(self.id == winner.id) userData else partnerData),
                  gainedCredits
                )

              module.bot.database.battleRepository.endBattle(battle)
            }

            val getMoveResultTexts: () -> String = {
              if (selfMoveFirst) {
                """
                ${getMoveResultText(moveResult, selfPokemon, partnerPokemon, moveData)}
                ${getMoveResultText(partnerMoveResult, partnerPokemon, selfPokemon, partnerMoveData)}
                """.trimIndent()
              } else {
                """
                ${getMoveResultText(partnerMoveResult, partnerPokemon, selfPokemon, partnerMoveData)}
                ${getMoveResultText(moveResult, selfPokemon, partnerPokemon, moveData)}
                """.trimIndent()
              }
            }

            val initiatorPokemon = if (self.id == battle.initiator.id) selfPokemon else partnerPokemon
            val opponentPokemon = if (self.id == battle.initiator.id) partnerPokemon else selfPokemon

            event.hook.sendMessageEmbeds(
              embedTemplates.normal(
                if(winner == null)
                  embedTemplates.translate(
                    "modules.battle.events.action.choseMove.complete.description",
                    mapOf(
                      "pokemonName" to selfPokemon.displayName,
                      "partnerPokemonName" to opponentPokemon.displayName,
                      "currentInitiatorHP" to self.pokemonStats.hp.toString(),
                      "pokemonHP" to initiatorPokemon.stats.hp.toString(),
                      "currentPartnerHP" to partner.pokemonStats.hp.toString(),
                      "partnerPokemonHP" to partnerPokemon.stats.hp.toString(),
                      "resultText" to getMoveResultTexts(),
                    )
                  )
                else
                  if(gainedCredits !== null)
                    embedTemplates.translate(
                      "modules.battle.events.action.choseMove.complete.endedWagerDescription",
                      mapOf(
                        "pokemonName" to selfPokemon.displayName,
                        "partnerPokemonName" to opponentPokemon.displayName,
                        "currentInitiatorHP" to self.pokemonStats.hp.toString(),
                        "pokemonHP" to initiatorPokemon.stats.hp.toString(),
                        "currentPartnerHP" to partner.pokemonStats.hp.toString(),
                        "partnerPokemonHP" to partnerPokemon.stats.hp.toString(),
                        "resultText" to getMoveResultTexts(),
                        "winnerName" to (if (winner.id == self.id) event.user.name else partnerUser.name),
                        "loserName" to (if (winner.id == self.id) partnerUser.name else event.user.name),
                        "winnerPokemonName" to (if (winner.id == self.id) selfPokemon else partnerPokemon).displayName,
                        "loserPokemonName" to (if (winner.id == self.id) partnerPokemon else selfPokemon).displayName,
                        "gainedXp" to gainedXp.toString(),
                        "gainedCredits" to gainedCredits.toString(),
                        "wager" to battle.wager.toString(),
                      )
                    )
                  else
                    embedTemplates.translate(
                      "modules.battle.events.action.choseMove.complete.endedDescription",
                      mapOf(
                        "pokemonName" to selfPokemon.displayName,
                        "partnerPokemonName" to opponentPokemon.displayName,
                        "currentInitiatorHP" to self.pokemonStats.hp.toString(),
                        "pokemonHP" to initiatorPokemon.stats.hp.toString(),
                        "currentPartnerHP" to partner.pokemonStats.hp.toString(),
                        "partnerPokemonHP" to partnerPokemon.stats.hp.toString(),
                        "resultText" to getMoveResultTexts(),
                        "winnerName" to (if (winner.id == self.id) event.user.name else partnerUser.name),
                        "loserName" to (if (winner.id == self.id) partnerUser.name else event.user.name),
                        "winnerPokemonName" to (if (winner.id == self.id) selfPokemon else partnerPokemon).displayName,
                        "loserPokemonName" to (if (winner.id == self.id) partnerPokemon else selfPokemon).displayName,
                        "gainedXp" to gainedXp.toString(),
                        "gainedCredits" to gainedCredits.toString()
                      )
                    ),
                embedTemplates.translate(
                  "modules.battle.events.action.choseMove.complete.title",
                  mapOf(
                    "initiatorName" to event.interaction.user.name,
                    "partnerName" to partnerUser.name,
                  )
                )
              )
              .setImage("attachment://battle.png")
              .build()
            )
              .addFile(
                BattleModule.getBattleImage(
                  battle,
                  initiatorPokemon.stats,
                  initiatorPokemon.shiny,
                  opponentPokemon.stats,
                  opponentPokemon.shiny
                ), "battle.png"
              )
              .also {
                @Suppress("CheckReturnValue")
                if (winner == null) {
                  it.addActionRow(
                    BattleModule.Buttons.getBattleActionRow(battle._id.toString())
                  )
                }
              }
              .queue()
          } else {
            event.hook.sendMessageEmbeds(
              embedTemplates.normal(
                embedTemplates.translate("modules.battle.events.action.choseMove.moveChosen.description"),
                embedTemplates.translate("modules.battle.events.action.choseMove.moveChosen.title")
              ).build()
            ).queue()
          }
        }
        is BattleModule.Buttons.BattleAction.EndBattle -> {
          if (battle.endedAtMillis != null) {
            event.replyEmbeds(
              embedTemplates.error(
                embedTemplates.translate("modules.battle.events.action.endBattle.errors.alreadyEnded")
              ).build()
            ).setEphemeral(true).queue()
          } else {
            val minRequiredTime = battle.startedAtMillis + BattleModule.BATTLE_TIMEOUT_MILLIS
            if (System.currentTimeMillis() < minRequiredTime) {
              event.replyEmbeds(
                embedTemplates.error(
                  embedTemplates.translate(
                    "modules.battle.events.action.endBattle.errors.timeError",
                  "time" to TimeFormat.RELATIVE.format(minRequiredTime)
                  )
                ).build()
              ).setEphemeral(true).queue()
            } else {
              module.bot.database.battleRepository.endBattle(battle)
              event.replyEmbeds(
                embedTemplates.normal(
                  embedTemplates.translate("modules.battle.events.action.endBattle.ended.description"),
                  embedTemplates.translate("modules.battle.events.action.endBattle.ended.title")
                ).build()
              ).queue()
            }
          }
        }
      }
    } catch (_: IndexOutOfBoundsException) {
    }
  }

  private fun getMoveResultText(
    moveResult: Battle.MoveResult, selfPokemon: OwnedPokemon, partnerPokemon: OwnedPokemon, moveData: MoveData
  ): String {
    if (moveResult.isMissed) return "**${selfPokemon.displayName}** used **${moveData.name}**, but it missed!"
    if (moveResult.nothingHappened) return "**${selfPokemon.displayName}** used **${moveData.name}**, but nothing happened."
    val sb = StringBuilder()
    sb.append("**${selfPokemon.displayName}** dealt **${moveResult.defenderDamage}** damage to **${partnerPokemon.displayName}**")
    sb.appendLine("${if (moveResult.selfDamage > 0)" and **${ moveResult.selfDamage}** damage to itself" else ""} using **${moveData.name}**!")
    if (moveResult.isCritical) sb.appendLine("It's a critical hit!")
    if (moveResult.typeEffectiveness >= 2) sb.appendLine("It's super effective!")
    else if (moveResult.typeEffectiveness == 0.5 || moveResult.typeEffectiveness == 0.25) sb.appendLine("It's not very effective!")
    return sb.toString()
  }
}
