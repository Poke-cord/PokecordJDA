package xyz.pokecord.bot.modules.battle.events

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.utils.MiscUtil
import org.litote.kmongo.coroutine.commitTransactionAndAwait
import org.litote.kmongo.eq
import xyz.pokecord.bot.core.managers.database.models.Auction
import xyz.pokecord.bot.core.structures.discord.EmbedTemplates
import xyz.pokecord.bot.core.structures.discord.base.Event
import xyz.pokecord.bot.modules.battle.BattleModule
import xyz.pokecord.utils.withCoroutineLock
import java.time.Instant

object BattleRequestActionEvent : Event() {
  override val name = "BattleRequestAction"

  val embedTemplates = EmbedTemplates()

  @Handler
  suspend fun onButtonClick(event: ButtonClickEvent) {
    try {
      val button = BattleModule.Buttons.fromComponentId(event.componentId)

      if (button !is BattleModule.Buttons.BattleRequest) return

      val initiatedChannelId = button.initiatedChannelId
      val initiatedAtMillis = button.initiatedAtMillis.toLongOrNull() ?: return

      val battleRequest =
        module.bot.database.battleRepository.findBattleRequest(initiatedChannelId, initiatedAtMillis) ?: return

      if (event.user.id != battleRequest.partnerId) {
        event.replyEmbeds(
          Embed {
            title = "You can't respond to this battle request"
            description = "You can only respond to a battle request that is meant for you."
          }
        ).setEphemeral(true).queue()
        return
      }
      event.message.delete().queue()

      if (button is BattleModule.Buttons.BattleRequest.Accept) {
        event.deferReply().queue()
        val initiator = event.jda.retrieveUserById(battleRequest.initiatorId).await()
        val partner = event.jda.retrieveUserById(battleRequest.partnerId).await()
        val initiatorData = module.bot.database.userRepository.getUser(battleRequest.initiatorId)
        val partnerData = module.bot.database.userRepository.getUser(battleRequest.partnerId)
        val initiatorPokemon = module.bot.database.pokemonRepository.getPokemonById(initiatorData.selected!!)!!
        val partnerPokemon = module.bot.database.pokemonRepository.getPokemonById(partnerData.selected!!)!!
        val battle = module.bot.database.battleRepository.acceptBattleRequest(
          battleRequest,
          initiatorPokemon.id,
          initiatorPokemon.stats,
          partnerPokemon.id,
          partnerPokemon.stats
        )

        if(battleRequest.wager !== null) {
          if(initiatorData.credits < battleRequest.wager || partnerData.credits < battleRequest.wager) {
            event.hook.sendMessageEmbeds(
              embedTemplates.error(
                embedTemplates.translate(
                  "modules.battle.events.request.accepted.errors.notEnoughCredits",
                  "user" to if(initiatorData.credits < battleRequest.wager) initiator.asMention else partner.asMention
                )
              ).build()
            ).queue()
            return
          }

          val session = module.bot.database.startSession()
          session.use {
            session.startTransaction()
            module.bot.database.userRepository.incCredits(initiatorData, -battleRequest.wager, session)
            module.bot.database.userRepository.incCredits(partnerData, -battleRequest.wager, session)
            session.commitTransactionAndAwait()
          }
        }

        event.hook.sendMessage("<@${battleRequest.initiatorId}>").addEmbeds(
          embedTemplates.normal(
            embedTemplates.translate(
              "modules.battle.events.request.accepted.description",
              mapOf(
                "initiatorPokemon" to initiatorPokemon.displayName,
                "partnerPokemon" to partnerPokemon.displayName,
                "currentInitiatorHP" to battle.initiator.pokemonStats.hp.toString(),
                "currentPartnerHP" to battle.partner.pokemonStats.hp.toString(),
                "initiatorPokemonHP" to initiatorPokemon.stats.hp.toString(),
                "partnerPokemonHP" to partnerPokemon.stats.hp.toString(),
              )
            ),
            embedTemplates.translate(
              "modules.battle.events.request.accepted.title",
              mapOf(
                "initiator" to initiator.name,
                "partner" to partner.name
              )
            ),
          )
            .setImage("attachment://battle.png")
            .setTimestamp(Instant.ofEpochMilli(battle.startedAtMillis))
            .build()
        )
          .addFile(
            BattleModule.getBattleImage(
              battle,
              initiatorPokemon.stats,
              initiatorPokemon.shiny,
              partnerPokemon.stats,
              partnerPokemon.shiny
            ), "battle.png"
          )
          .addActionRow(
            BattleModule.Buttons.getBattleActionRow(battle._id.toString())
          )
          .queue()
      } else {
        module.bot.database.battleRepository.rejectBattleRequest(battleRequest)
        event.replyEmbeds(
          embedTemplates.normal(
            embedTemplates.translate("modules.battle.events.request.rejected.description"),
            embedTemplates.translate("modules.battle.events.request.rejected.title")
          ).build()
        ).setEphemeral(true).queue()
      }
    } catch (_: IndexOutOfBoundsException) {
    }
  }
}
