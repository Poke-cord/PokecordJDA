package xyz.pokecord.bot.modules.battle.events

import dev.minn.jda.ktx.Embed
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
            // TODO: use embed templates here
            event.hook.sendMessageEmbeds(Embed {
              title = "Error"
              color = EmbedTemplates.Color.RED.code
              description = "You are not in this battle!"
            }).queue()
            return
          }
          val self = if (interactionTrainer == battle.initiator) battle.initiator else battle.partner
          val partner = if (interactionTrainer == battle.initiator) battle.partner else battle.initiator
          val partnerUser = event.jda.retrieveUserById(partner.id).await()

          val partnerData = module.bot.database.userRepository.getUser(partner.id)

          val pokemon = module.bot.database.pokemonRepository.getPokemonById(userData.selected!!)!!
          val partnerPokemon = module.bot.database.pokemonRepository.getPokemonById(partnerData.selected!!)!!

          event.hook.sendMessageEmbeds(Embed {
            title = "Battle featuring ***You*** VS. ***${partnerUser.name}***"
            // TODO: use translator somehow
            description = """
                  **Your** ${pokemon.displayName}
                  Level ${pokemon.level} │ ${self.pokemonStats.hp}/${pokemon.stats.hp} HP
                  
                  ***${partnerUser.name}'s*** ${partnerPokemon.displayName}
                  Level ${partnerPokemon.level} │ ${partner.pokemonStats.hp}/${partnerPokemon.stats.hp} HP
                  
                  > **Select a move to execute.**
                  """.trimIndent()
            image = "attachment://battle.png"
            timestamp = Instant.ofEpochMilli(battle.startedAtMillis)
          }).addActionRow(pokemon.moves.toSet().mapNotNull {
            if (it == 0) return@mapNotNull null

            Button.primary(
              BattleModule.Buttons.BattleAction.ChooseMove(battleId, it.toString()).toString(),
              MoveData.getById(it)!!.name
            )
          }).queue()
        }
        is BattleModule.Buttons.BattleAction.ChooseMove -> {
          if (battle.endedAtMillis != null) {
            event.replyEmbeds(Embed {
              title = "Battle Already Ended"
              color = EmbedTemplates.Color.RED.code
              description = "This battle has already ended!"
            }).setEphemeral(true).queue()
            return
          }
          val moveId = button.moveId.toIntOrNull() ?: return
          val moveData = MoveData.getById(moveId) ?: return

          val (self, partner) = battle.getTrainers(event.user.id)
          if (self.pendingMove != null) {
            event.replyEmbeds(Embed {
              title = "Move Already Pending"
              color = EmbedTemplates.Color.RED.code
              description = "You already have a move pending!"
            }).setEphemeral(true).queue()
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
              module.bot.database.battleRepository.endBattle(battle)
            }

            val getMoveResultTexts: () -> String = {
              if (selfMoveFirst) { """
                ${getMoveResultText(moveResult, selfPokemon, partnerPokemon, moveData)}
                
                ${getMoveResultText(partnerMoveResult, partnerPokemon, selfPokemon, partnerMoveData)}
                """.trimIndent()
              }
              else { """
                ${getMoveResultText(partnerMoveResult, partnerPokemon, selfPokemon, partnerMoveData)}
                
                ${getMoveResultText(moveResult, selfPokemon, partnerPokemon, moveData)}
                """.trimIndent()
              }
            }

            val initiatorPokemon = if (self.id == battle.initiator.id) selfPokemon else partnerPokemon
            val opponentPokemon = if (self.id == battle.initiator.id) partnerPokemon else selfPokemon

            event.hook.sendMessageEmbeds(Embed {
              title = "Battle featuring ***${event.interaction.user.name}*** VS. ***${partnerUser.name}***"
              description = """
                ${getMoveResultTexts()}
                
                ${if (winner != null)"""**${if (winner.id == self.id) event.user.name else partnerUser.name} won the battle!!**
                —
                Their ${(if (winner.id == self.id) selfPokemon else partnerPokemon).displayName} gained $gainedXp XP!
                *${if (winner.id == self.id) partnerUser.name else event.user.name}'s* ${(if (winner.id == self.id) partnerPokemon else selfPokemon).displayName} fainted!
                —
                """.trimIndent()
                else """""".trimIndent()}""".trimIndent()
              image = "attachment://battle.png"
              field(
                "*${event.interaction.user.name}'s* ${selfPokemon.displayName}",
                "Level ${selfPokemon.level} │ ${self.pokemonStats.hp}/${selfPokemon.stats.hp} HP",
                true
              )
              field(
                "*${partnerUser.name}'s* ${partnerPokemon.displayName}",
                "Level ${partnerPokemon.level} │ ${partner.pokemonStats.hp}/${partnerPokemon.stats.hp} HP",
                true
              )
            })
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
            event.hook.sendMessageEmbeds(Embed {
              title = "Move Locked In"
              description = "The move will be executed when your opponent is ready."
            }).queue()
          }
        }
        is BattleModule.Buttons.BattleAction.EndBattle -> {
          if (battle.endedAtMillis != null) {
            event.replyEmbeds(Embed {
              title = "Battle Ended"
              description = "This battle has already ended."
            }).setEphemeral(true).queue()
          } else {
            val minRequiredTime = battle.startedAtMillis + BattleModule.BATTLE_TIMEOUT_MILLIS
            if (System.currentTimeMillis() < minRequiredTime) {
              event.replyEmbeds(Embed {
                title = "Failed to End Battle"
                description =
                  "Thou shall not show weakness. The battle can be ended ${TimeFormat.RELATIVE.format(minRequiredTime)}."
              }).setEphemeral(true).queue()
            } else {
              module.bot.database.battleRepository.endBattle(battle)
              event.replyEmbeds(Embed {
                title = "Battle Ended"
                description = "This battle has been ended."
              }).queue()
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
    if (moveResult.isMissed) return "${selfPokemon.displayName} used ${moveData.name}!\n> ***${moveData.name} missed!***"
    if (moveResult.nothingHappened) return "${selfPokemon.displayName} used ${moveData.name}!\n> ***Nothing happened...***"

    val moveHit = "${selfPokemon.displayName} used ${moveData.name}!\n> ***${moveData.name} dealt " +
        "${if (moveResult.selfDamage > 0)"${moveResult.selfDamage} damage to the dealer and" else ""} ${moveResult.defenderDamage} damage to the opponent.*** " +
        (if (moveResult.isCritical)"***It's a critical hit!*** " else "") +
        (if (moveResult.typeEffectiveness >= 2)"***It's super effective!***" else "") +
        (if (moveResult.typeEffectiveness == 0.5 || moveResult.typeEffectiveness == 0.25)"***It's not very effective...***" else "")
    return moveHit

    /*
    val sb = StringBuilder()
    //sb.append("**${selfPokemon.displayName}** dealt **${moveResult.defenderDamage}** damage to **${partnerPokemon.displayName}**")
    //sb.appendLine("${if (moveResult.selfDamage > 0)" and **${ moveResult.selfDamage}** damage to itself" else ""} using **${moveData.name}**!")
    sb.append("${selfPokemon.displayName} used ${moveData.name}!\n> ***${moveData.name} dealt")
    sb.appendLine("${if (moveResult.selfDamage > 0)" ${moveResult.selfDamage} damage to the dealer and" else ""} ${moveResult.defenderDamage} damage to the opponent.*** " +
        (if (moveResult.isCritical)"***It's a critical hit!*** " else "") +
        (if (moveResult.typeEffectiveness >= 2)"***It's super effective!***" else "") +
        (if (moveResult.typeEffectiveness == 0.5 || moveResult.typeEffectiveness == 0.25)"***It's not very effective...***" else "")
    )
    //if (moveResult.isCritical) sb.appendLine("It's a critical hit!") //Moved up
    //if (moveResult.typeEffectiveness >= 2) sb.appendLine("It's super effective!") //Moved up
    //else if (moveResult.typeEffectiveness == 0.5 || moveResult.typeEffectiveness == 0.25) sb.appendLine("It's not very effective...***") //Moved up
    return sb.toString()
    */
  }
}
