package xyz.pokecord.bot.modules.pokemon.gift

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.entities.User
import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.Confirmation
import xyz.pokecord.bot.utils.PokemonResolvable

object GiftPokemonCommand : Command() {
  override val name = "Pokemon"

  override var aliases = arrayOf("p")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument receiver: User?,
    @Argument(name = "pokemon") pokemonResolvable: PokemonResolvable?
  ) {
    if (!context.hasStarted(true)) return

    // TODO: check battle and trade state

    if (receiver == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.pokemon.commands.gift.errors.mentionUser")
        ).build()
      ).queue()
      return
    }

    if (receiver.isBot) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.pokemon.commands.gift.errors.botReceiver")
        ).build()
      ).queue()
      return
    }

    if (receiver.id == context.author.id) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.pokemon.commands.gift.errors.selfReceiver")
        ).build()
      ).queue()
      return
    }

    var receiverData = module.bot.database.userRepository.getUser(receiver)
    if (receiverData.selected == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate(
            "misc.errors.userHasNotStarted",
            mapOf(
              "user" to receiver.asMention,
              "prefix" to context.getPrefix()
            )
          )
        ).build()
      ).queue()
      return
    }

    var userData = context.getUserData()
    val pokemon = context.resolvePokemon(context.author, userData, pokemonResolvable)

    when {
      pokemon == null -> {
        context.reply(
          context.embedTemplates.error(
            context.translate("misc.errors.pokemonNotFound")
          ).build()
        ).queue()
        return
      }
      userData.selected == pokemon._id -> {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.pokemon.commands.gift.errors.selectedPokemon")
          ).build()
        ).queue()
        return
      }
      pokemon.favorite -> {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.pokemon.commands.gift.errors.favoritePokemon")
          ).build()
        ).queue()
        return
      }
      else -> {
        val confirmation = Confirmation(context, context.author.id)
        val confirmed = confirmation.result(
          context.embedTemplates.confirmation(
            context.translate(
              "modules.pokemon.commands.gift.embeds.confirmation.pokemon",
              mapOf(
                "sender" to context.author.asMention,
                "receiver" to receiver.asMention,
                "level" to pokemon.level.toString(),
                "ivPercentage" to pokemon.ivPercentage,
                "pokemon" to context.translator.pokemonDisplayName(pokemon, false)
              )
            ),
            context.translate("modules.pokemon.commands.gift.embeds.confirmation.title")
          )
        )

        if (confirmed) {
          val session = module.bot.database.startSession()
          module.bot.cache.withGiftLock(context.author.id, receiver.id) {
            userData = context.getUserData(true)
            receiverData = module.bot.database.userRepository.getUser(receiver)
            session.use { clientSession ->
              clientSession.startTransaction()
              module.bot.database.pokemonRepository.giftPokemon(userData, receiverData, pokemon, clientSession)
              module.bot.database.userRepository.giftPokemon(userData, receiverData, pokemon, clientSession)
              clientSession.commitTransactionAndAwait()
            }
          }
          try {
            val privateChannel = receiver.openPrivateChannel().submit().await()
            privateChannel.sendMessageEmbeds(
              context.embedTemplates.normal(
                context.translate(
                  "modules.pokemon.commands.gift.embeds.giftReceived.pokemon",
                  mapOf(
                    "sender" to context.author.asMention,
                    "ivPercentage" to pokemon.ivPercentage,
                    "level" to pokemon.level.toString(),
                    "pokemon" to context.translator.pokemonDisplayName(pokemon, false)
                  )
                ),
                context.translate("modules.pokemon.commands.gift.embeds.giftReceived.title")
              ).build()
            ).await()
          } catch (_: Exception) {
          }

          confirmation.sentMessage!!.editMessageEmbeds(
            context.embedTemplates.normal(
              context.translate(
                "modules.pokemon.commands.gift.embeds.giftSent.pokemon",
                mapOf(
                  "sender" to context.author.asMention,
                  "receiver" to receiver.asMention,
                  "level" to pokemon.level.toString(),
                  "ivPercentage" to pokemon.ivPercentage,
                  "pokemon" to context.translator.pokemonDisplayName(pokemon, false)
                )
              ),
              context.translate("modules.pokemon.commands.gift.embeds.giftSent.title")
            ).build()
          ).queue()
        } else {
          confirmation.sentMessage!!.editMessageEmbeds(
            context.embedTemplates.normal(
              context.translate("modules.pokemon.commands.gift.embeds.cancelled.description"),
              context.translate("modules.pokemon.commands.gift.embeds.cancelled.title")
            ).build()
          ).queue()
        }
      }
    }
  }
}