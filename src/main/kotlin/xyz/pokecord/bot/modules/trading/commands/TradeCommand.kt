package xyz.pokecord.bot.modules.trading.commands

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.entities.User
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.utils.Confirmation
import xyz.pokecord.bot.utils.PokemonResolvable
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
  class AddCommand: ParentCommand() {
    override val name = "add"

    @Executor
    suspend fun execute(
      context: ICommandContext
    ) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.trading.commands.add.errors.noTypeProvided")
        ).build()
      ).queue()
    }

    @ChildCommand
    class AddCredits: Command() {
      override val name = "credits"
      override var aliases = arrayOf("c", "creds", "credits", "credit")

      @Executor
      suspend fun execute(
        context: ICommandContext,
        @Argument amount: Int?
      ) {
        if (!context.hasStarted(true)) return

        println(amount)

        val tradeState = context.getTradeState()
        if(tradeState == null) {
          context.reply(
            context.embedTemplates.error(
              context.translate("modules.trading.commands.add.errors.notInTrade")
            ).build()
          ).queue()
          return
        }

        if(amount == null) {
          context.reply(
            context.embedTemplates.error(
              context.translate("modules.trading.commands.add.errors.noNumberCredits")
            ).build()
          ).queue()
          return
        }

        val userData = context.getUserData()
        if(amount > userData.credits) {
          context.reply(
            context.embedTemplates.error(
              context.translate("modules.trading.commands.add.errors.notEnoughCredits")
            ).build()
          ).queue()
          return
        }

        context.bot.database.userRepository.incCredits(userData, -amount)
        context.bot.database.tradeRepository.incCredits(tradeState, context.author.id, amount)

        context.reply(
          context.embedTemplates.normal(
            context.translate(
              "modules.trading.commands.add.embeds.addCredits.description",
              "credits" to amount.toString()
            ),
            context.translate("modules.trading.commands.add.embeds.addCredits.title")
          ).build()
        ).queue()
      }
    }

    @ChildCommand
    class AddPokemon: Command() {
      override val name = "pokemon"
      override var aliases = arrayOf("p", "pkmn", "pokemon", "poke")

      @Executor
      suspend fun execute(
        context: ICommandContext,
        @Argument pokemon: PokemonResolvable?
      ) {
        if (!context.hasStarted(true)) return

        val tradeState = context.getTradeState()
        if(tradeState == null) {
          context.reply(
            context.embedTemplates.error(
              context.translate("modules.trading.commands.add.errors.notInTrade")
            ).build()
          ).queue()
        } else {
          if(pokemon == null) {
            context.reply(
              context.embedTemplates.error(
                context.translate("modules.trading.commands.add.errors.noNumberPokemon")
              ).build()
            ).queue()
            return
          }

          val userData = context.getUserData()
          val selectedPokemon = context.resolvePokemon(context.author, userData, pokemon)

          if(selectedPokemon == null) {
            context.reply(
              context.embedTemplates.error(
                context.translate(
                  "modules.trading.commands.add.errors.noPokemonFound",
                  "index" to pokemon.toString()
                )
              ).build()
            ).queue()
          } else {
            val authorTradeState = if(tradeState.initiator.userId == context.author.id) tradeState.initiator else tradeState.receiver
            if (authorTradeState.pokemon.size >= 20) {
              context.reply(
                context.embedTemplates.error(
                  context.translate("modules.trading.commands.add.errors.maxPokemonCount")
                ).build()
              ).queue()
            }

            val transfer = selectedPokemon.transferable(context.bot.database)
            if (transfer != OwnedPokemon.TransferStates.SUCCESS) {
              context.reply(
                context.embedTemplates.error(
                  transfer.errMessage!!,
                  context.translate("modules.trading.commands.add.errors.notTransferableTitle")
                ).build()
              ).queue()
            }

            context.bot.database.tradeRepository.addPokemon(tradeState, context.author.id, selectedPokemon._id)

            context.reply(
              context.embedTemplates.normal(
                context.translate(
                  "modules.trading.commands.add.embeds.addPokemon.description",
                  "pokemon" to selectedPokemon.displayName
                ),
                context.translate("modules.trading.commands.add.embeds.addPokemon.title")
              ).build()
            ).queue()
          }
        }
      }
    }
  }

  // @ChildCommand
  // class RemoveCommand: Command() {}

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

  // @ChildCommand
  // class ConfirmCommand: Command() {}
}