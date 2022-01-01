package xyz.pokecord.bot.modules.pokemon.commands

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.Confirmation
import xyz.pokecord.bot.utils.PokemonResolvable

class ReleaseCommand : Command() {
  override val name = "Release"

  override var aliases = arrayOf("r")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(name = "pokemon", optional = true) pokemonResolvable: PokemonResolvable?
  ) {
    if (!context.hasStarted(true)) return

    val userData = context.getUserData()

    if (pokemonResolvable == null) {
      context.reply(context.embedTemplates.error(context.translate("misc.errors.pokemonNotFound")).build())
        .queue()
      return
    }

    val pokemon = context.resolvePokemon(context.author, userData, pokemonResolvable)
    when {
      pokemon == null -> {
        context.reply(context.embedTemplates.error(context.translate("misc.errors.pokemonNotFound")).build())
          .queue()
        return
      }
      pokemon._id == userData.selected -> {
        context.reply(
          context.embedTemplates.error(context.translate("modules.pokemon.commands.release.errors.selectedPokemon"))
            .build()
        )
          .queue()
        return
      }
      pokemon.favorite -> {
        context.reply(
          context.embedTemplates.error(context.translate("modules.pokemon.commands.release.errors.favoritePokemon"))
            .build()
        )
          .queue()
        return
      }
      else -> {
        val confirmation = Confirmation(context)
        val result =
          confirmation.result(
            context.embedTemplates.confirmation(
              context.translate(
                "modules.pokemon.commands.release.embeds.confirmation.embed.description",
                mapOf(
                  "level" to pokemon.level.toString(),
                  "pokemon" to context.translator.pokemonDisplayName(pokemon),
                  "ivPercentage" to pokemon.ivPercentage
                )
              ),
              context.translate("modules.pokemon.commands.release.embeds.confirmation.embed.title")
            )
              .setFooter(
                context.translate(
                  "misc.confirmation.timeoutText",
                  "timeout" to (confirmation.timeout / 1_000).toString()
                )
              )
          )

        if (!result) {
          confirmation.sentMessage!!.editMessageEmbeds(
            context.embedTemplates.normal(
              context.translate("modules.pokemon.commands.release.embeds.cancelled.description"),
              context.translate("modules.pokemon.commands.release.embeds.cancelled.title")
            ).build()
          ).queue()
          return
        }

        val session = module.bot.database.startSession()
        session.use { clientSession ->
          clientSession.startTransaction()
          module.bot.database.pokemonRepository.releasePokemon(pokemon, clientSession)
          module.bot.database.userRepository.releasePokemon(userData, pokemon, clientSession)
          clientSession.commitTransactionAndAwait()
        }

        confirmation.sentMessage!!.editMessageEmbeds(
          context.embedTemplates.normal(
            context.translate(
              "modules.pokemon.commands.release.embeds.released.description",
              mapOf(
                "level" to pokemon.level.toString(),
                "pokemon" to context.translator.pokemonDisplayName(pokemon),
                "ivPercentage" to pokemon.ivPercentage
              )
            ),
            context.translate("modules.pokemon.commands.release.embeds.released.title")
          )
            .setColor(pokemon.data.species.color.colorCode)
            .build()
        ).queue()
      }
    }
  }
}
