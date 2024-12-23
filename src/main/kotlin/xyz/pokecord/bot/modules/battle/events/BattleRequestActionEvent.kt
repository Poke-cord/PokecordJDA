package xyz.pokecord.bot.modules.battle.events

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import xyz.pokecord.bot.core.structures.discord.base.Event
import xyz.pokecord.bot.modules.battle.BattleModule
import java.time.Instant

object BattleRequestActionEvent : Event() {
  override val name = "BattleRequestAction"

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
      val userData = module.bot.database.userRepository.getUser(event.user)
      if (userData.blacklisted) {
        module.bot.database.battleRepository.rejectBattleRequest(battleRequest)
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
        // TODO: use context-less EmbedTemplates here when it's available after merge
        event.hook.sendMessage("<@${battleRequest.initiatorId}>").addEmbeds(
          Embed {
            title = "Battle featuring ***${initiator.name}*** VS. ***${partner.name}***"
            // TODO: use translator somehow
            image = "attachment://battle.png"
            timestamp = Instant.ofEpochMilli(battle.startedAtMillis)
            field(
              "*${initiator.name}'s* ${initiatorPokemon.displayName}",
              "Level ${initiatorPokemon.level} │ ${battle.initiator.pokemonStats.hp}/${initiatorPokemon.stats.hp} HP",
              true
            )
            field(
              "*${partner.name}'s* ${partnerPokemon.displayName}",
              "Level ${partnerPokemon.level} │ ${battle.partner.pokemonStats.hp}/${partnerPokemon.stats.hp} HP",
              true
            )
          }
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
          Embed {
            title = "Battle Request Rejected"
            description = "The battle request has been rejected."
          }
        ).setEphemeral(true).queue()
      }
    } catch (_: IndexOutOfBoundsException) {
    }
  }
}
