package xyz.pokecord.bot.modules.trading.commands

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.entities.User
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.utils.Confirmation
import java.util.concurrent.TimeUnit

class TradeCommand : ParentCommand() {
  override val name = "Trade"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(optional = false, name = "partner") partner: User?
  ) {
    if (!context.hasStarted(true)) return

    val tradeState = context.getTradeState()
    if(tradeState != null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.commands.trade.errors.alreadyInTrade")
        ).build()
      ).queue()
      return
    }

    if(partner == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.commands.trade.errors.noPartnerTagged")
        ).build()
      ).queue()
      return
    } else if(partner.id == context.author.id) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.commands.trade.errors.tradingYourself")
        ).build()
      ).queue()
      return
    }

    val partnerData = context.bot.database.userRepository.getUser(partner)
    if(partnerData.selected == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate(
            "modules.trading.commands.trade.errors.partnerHasntStarted",
            "partner" to partner.asMention
          )
        ).build()
      ).queue()
    }

    val mentionMsg = context.channel.sendMessage(partner.asMention).await()
    mentionMsg.delete().queueAfter(3000, TimeUnit.MILLISECONDS)

    val confirmation = Confirmation(context, partner.id, 30_000)
    val confirmed = confirmation.result(
      context.embedTemplates.confirmation(
        context.translate(
          "modules.trading.commands.trade.confirmation.description",
          mapOf(
            "trader" to context.author.asMention,
            "traded" to partner.asMention
          )
        ),
        context.translate(
          "modules.trading.commands.trade.confirmation.title"
        )
      )
    )

    if(confirmed) {
      context.bot.database.tradeRepository.createTrade(context.author.id, partner.id)
      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.trading.commands.trade.tradeStarted.description",
            mapOf(
              "partner" to partner.asMention,
              "author" to context.author.asMention
            )
          ),
          context.translate("modules.trading.commands.trade.tradeStarted.title")
        ).build()
      ).queue()
    } else {
      context.reply(
        context.embedTemplates.normal(
          context.translate("modules.trading.commands.trade.tradeCancelled.description"),
          context.translate("modules.trading.commands.trade.tradeCancelled.title")
        ).build()
      ).queue()
    }
  }

  @ChildCommand
  class CancelCommand: Command() {
    override val name = "cancel"

    @Executor
    suspend fun execute(
      context: ICommandContext
    ) {
      if (!context.hasStarted(true)) return

      val tradeState = context.getTradeState()
      if(tradeState == null) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.trading.commands.cancel.errors.notInTrade")
          ).build()
        ).queue()
        return
      }

      val initiator = if(tradeState.initiator.userId == context.author.id) tradeState.initiator else tradeState.receiver
      val partner = if(tradeState.initiator.userId == context.author.id) tradeState.receiver else tradeState.initiator

      val partnerUser = context.jda.retrieveUserById(partner.userId).await()
      val partnerData = context.bot.database.userRepository.getUser(partnerUser)

      context.bot.database.userRepository.incCredits(partnerData, partner.credits)
      context.bot.database.userRepository.incCredits(context.getUserData(), initiator.credits)

      context.bot.database.tradeRepository.deleteTrade(initiator.userId)

      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.trading.commands.cancel.embeds.cancelTrade.description",
            mapOf(
              "author" to context.author.asMention,
              "partner" to partnerUser.asMention
            )
          ),
          context.translate("modules.trading.commands.cancel.embeds.cancelTrade.title")
        ).build()
      ).queue()
    }
  }

  @ChildCommand
  class AddCommand: Command() {
    override val name = "add"

    enum class Types(val identifier: String) {
      POKEMON("pokemon"),
      CREDITS("credits")
    }

    val pokemonTypes = listOf("p", "pkmn", "pokemon", "poke")
    val creditTypes = listOf("c", "creds", "credits", "credit")

    @Executor
    suspend fun execute(
      context: ICommandContext,
      @Argument(description = "pokemon or credits") type: String?,
      @Argument number: Int?
    ) {
      if (!context.hasStarted(true)) return

      val tradeState = context.getTradeState()
      if(tradeState == null) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.trading.commands.add.errors.notInTrade")
          ).build()
        ).queue()
        return
      }

      val authorTradeData = if(tradeState.initiator.userId == context.author.id) tradeState.initiator else tradeState.receiver

      if(type == null) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.trading.commands.add.errors.noTypeProvided")
          ).build()
        ).queue()
      } else {
        val tradeType = if(pokemonTypes.contains(type.lowercase())) Types.POKEMON else Types.CREDITS

        if(number == null) {
          context.reply(
            context.embedTemplates.error(
              if(tradeType == Types.CREDITS)
                context.translate("modules.trading.commands.add.errors.noNumberCredits")
              else
                context.translate("modules.trading.commands.add.errors.noNumberPokemon")
            ).build()
          ).queue()
          return
        }

        if(tradeType == Types.CREDITS) {
          val userData = context.getUserData()
          if(number > userData.credits) {
            context.reply(
              context.embedTemplates.error(
                context.translate("modules.trading.commands.add.errors.notEnoughCredits")
              ).build()
            ).queue()
            return
          }

          context.bot.database.userRepository.incCredits(userData, -number)
          context.bot.database.tradeRepository.incCredits(tradeState, context.author.id, number)

          context.reply(
            context.embedTemplates.normal(
              context.translate(
                "modules.trading.commands.add.embeds.addCredits.description",
                "credits" to number.toString()
              ),
              context.translate("modules.trading.commands.add.embeds.addCredits.title")
            ).build()
          ).queue()
        } else {
          if(authorTradeData.pokemon.size >= 20) {
            context.reply(
              context.embedTemplates.error(
                context.translate("modules.trading.commands.add.errors.maxPokemonCount")
              ).build()
            ).queue()
          }

          val selectedPokemon = context.bot.database.pokemonRepository.getPokemonByIndex(context.author.id, number)
          if(selectedPokemon == null) {
            context.reply(
              context.embedTemplates.error(
                context.translate(
                  "modules.trading.commands.add.errors.noPokemonFound",
                  "index" to number.toString()
                )
              ).build()
            ).queue()
            return
          }
          // Add pokemon to trade
        }
      }
    }
  }


  @ChildCommand
  class StatusCommand: Command() {
    override val name = "status"

    @Executor
    suspend fun execute(
      context: ICommandContext
    ) {
      if (!context.hasStarted(true)) return

      val tradeState = context.getTradeState()
      if(tradeState == null) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.trading.commands.status.errors.notInTrade")
          ).build()
        ).queue()
        return
      }

      val partner = if(tradeState.initiator.userId == context.author.id) tradeState.receiver else tradeState.initiator
      val initiator = if(tradeState.initiator.userId == context.author.id) tradeState.initiator else tradeState.receiver

      val partnerUser = context.jda.retrieveUserById(partner.userId).await()
      if(partnerUser == null) {
        val confirmation = Confirmation(context, initiator.userId)
        val confirmed = confirmation.result(
          context.embedTemplates.confirmation(
            context.translate("modules.trading.commands.status.errors.noPartnerFound.description"),
            context.translate("modules.trading.commands.status.errors.noPartnerFound.title")
          )
        )

        if(confirmed) {
          context.bot.database.tradeRepository.deleteTrade(context.author.id)
          context.reply(
            context.embedTemplates.normal(
              context.translate("modules.trading.commands.status.embeds.tradeEnded.description"),
              context.translate("modules.trading.commands.status.embeds.tradeEnded.title")
            ).build()
          ).queue()
        }

        return
      }

      context.reply(
        context.embedTemplates.normal(
          "testing"
        ).build()
      ).queue()
    }
  }
}