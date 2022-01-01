package xyz.pokecord.bot.modules.battle.commands

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.components.ActionRow
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.modules.battle.BattleModule

object BattleCommand : Command() {
  override val name = "Battle"
  override var aliases = arrayOf("duel")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument partner: User?
  ) {
    if (!context.hasStarted(true)) return

    val selfCurrentBattle = context.bot.database.battleRepository.getUserCurrentBattle(context.author)
    if (selfCurrentBattle != null) {
      val initiatorData = context.bot.database.userRepository.getUser(selfCurrentBattle.initiator.id)
      val partnerData = context.bot.database.userRepository.getUser(selfCurrentBattle.partner.id)
      val initiatorPokemon = context.bot.database.pokemonRepository.getPokemonById(initiatorData.selected!!)!!
      val partnerPokemon = context.bot.database.pokemonRepository.getPokemonById(partnerData.selected!!)!!
      context
        .addAttachment(
          BattleModule.getBattleImage(selfCurrentBattle, initiatorPokemon.stats, partnerPokemon.stats),
          "battle.png"
        )
        .addActionRows(
          ActionRow.of(BattleModule.Buttons.getBattleActionRow(selfCurrentBattle._id.toString()))
        )
        .reply(
          context.embedTemplates.error(
            context.translate("modules.battle.commands.battle.errors.alreadyBattling")
          )
            .setImage("attachment://battle.png")
            .build()
        )
        .queue()
      return
    }

    if (partner == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.battle.commands.battle.errors.noPartnerMentioned")
        ).build()
      ).queue()
      return
    }

    if (partner.id == context.author.id) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.battle.commands.battle.errors.selfBattle")
        ).build()
      ).queue()
      return
    }

    val partnerCurrentBattle = context.bot.database.battleRepository.getUserCurrentBattle(partner)
    if (partnerCurrentBattle != null) {
      context.reply(
        context.embedTemplates.error(
          context.translate(
            "modules.battle.commands.battle.errors.partnerAlreadyBattling",
            "partner" to partner.asMention
          )
        ).build()
      ).queue()
      return
    }

    val partnerData = context.bot.database.userRepository.getUser(partner)
    if (partnerData.selected == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate(
            "modules.battle.commands.battle.errors.partnerHasNotStarted",
            "partner" to partner.asMention
          )
        ).build()
      ).queue()
      return
    }

    // TODO: prevent people from multiple starting multiple requests, lock maybe? or maybe the single-command-execution check is enough?

    val userData = context.getUserData()
    val pokemon = context.bot.database.pokemonRepository.getPokemonById(userData.selected!!)
    val partnerPokemon = context.bot.database.pokemonRepository.getPokemonById(partnerData.selected!!)

    if (pokemon == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.battle.commands.battle.errors.pokemonNotFound")
        ).build()
      ).queue()
      return
    }
    if (partnerPokemon == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.battle.commands.battle.errors.partnerPokemonNotFound")
        ).build()
      ).queue()
      return
    }

    if (pokemon.moves.all { it == 0 }) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.battle.commands.battle.errors.noPokemonMoves")
        ).build()
      ).queue()
      return
    }

    if (partnerPokemon.moves.all { it == 0 }) {
      context.reply(
        context.embedTemplates.error(
          context.translate(
            "modules.battle.commands.battle.errors.noPartnerPokemonMoves",
            "partner" to partner.asMention
          )
        ).build()
      ).queue()
      return
    }

    val battleRequest = context.bot.database.battleRepository.initiateBattleRequest(
      context.author.id,
      partner.id,
      context.channel.id
    )

    context.channel.sendMessageEmbeds(
      context.embedTemplates.normal(
        context.translate(
          "modules.battle.commands.battle.embeds.battleRequest.description",
          mapOf(
            "initiator" to context.author.asMention,
            "partner" to partner.asMention,
          )
        ),
        context.translate("modules.battle.commands.battle.embeds.battleRequest.title")
      ).build()
    ).setActionRow(
      BattleModule.Buttons.getBattleRequestActionRow(battleRequest)
    ).queue()
  }
}
