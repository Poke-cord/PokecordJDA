package xyz.pokecord.bot.modules.release.commands

import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.Confirmation
import xyz.pokecord.bot.utils.PokemonResolvable
import xyz.pokecord.bot.core.structures.discord.MessageCommandContext
import xyz.pokecord.bot.modules.trading.TradingModule
import kotlin.math.ceil
import kotlin.math.floor

object ReleaseAddCommand : Command() {
  override val name: String = "add"
  override var aliases = arrayOf("a")

  @Executor
  suspend fun execute(
    context: ICommandContext,
  ) {
    if (!context.hasStarted(true)) return

    if (context !is MessageCommandContext) return

    val releaseState = context.getTradeState()
    val userData = context.getUserData()

    val numbers = charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
    val indexOfFirstMatch = context.event.message.contentRaw.indexOfAny(
      numbers, 0, false
    )

    if (indexOfFirstMatch == -1) {
      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.pokemon.commands.release.embeds.center.embed.description",
            mapOf(
              "prefix" to context.getPrefix()
            )
          ),
          context.translate("modules.pokemon.commands.release.embeds.center.embed.title")
        ).build()
      ).queue()

      return
    }

    val input = context.event.message.contentRaw.drop(indexOfFirstMatch).trim()
    val pkr = mutableListOf<PokemonResolvable>()

    if (input.contains('-', true)) {
      val regex = """[0-9]+""".toRegex()
      val matches = regex.findAll(input).map { it.value }.toList()

      if (matches.size != 2) {
        context.reply(
          context.embedTemplates.normal(
            context.translate(
              "modules.pokemon.commands.release.embeds.center.embed.description",
              mapOf(
                "prefix" to context.getPrefix()
              )
            ),
            context.translate("modules.pokemon.commands.release.embeds.center.embed.title")
          ).build()
        ).queue()

        return
      }

      val startingNumber: Int = matches[0].toInt()
      val endingNumber: Int = matches[1].toInt()

      if (endingNumber - startingNumber > 999 || startingNumber > endingNumber) {
        context.reply(
          context.embedTemplates.error(
            context.translate(
              "modules.pokemon.commands.release.errors.notInRange"
            )
          ).build()
        ).queue()

        return
      }

      if (context.event.message.contentRaw.contains(" iv ", ignoreCase = true)) {
        val startingIv = floor((startingNumber.toDouble() / 100) * 186).toInt()
        val endingIv = ceil((endingNumber.toDouble() / 100) * 186).toInt()

        for (i in startingIv..endingIv) {

          val pokemonRes = PokemonResolvable.Ivs(i)
          val pokemon = context.resolvePokemon(context.author, userData, pokemonRes)

          if(pokemon != null) {
            pkr.add(pokemonRes)
          }
        }
      }
      else {
        for (i in startingNumber..endingNumber) {
          val pokemonRes = PokemonResolvable.Int(i)
          val pokemon = context.resolvePokemon(context.author, userData, pokemonRes)

          if(pokemon != null) {
            pkr.add(pokemonRes)
          }
        }
      }
    } else {
      val inputArr = input.split(',')

      inputArr.forEach {
        val pokemonIndex = it.trim().toIntOrNull()

        if (pokemonIndex == null) {
          context.reply(
            context.embedTemplates.normal(
              context.translate(
                "modules.pokemon.commands.release.embeds.center.embed.description",
                mapOf(
                  "prefix" to context.getPrefix()
                )
              ),
              context.translate("modules.pokemon.commands.release.embeds.center.embed.title")
            ).build()
          ).queue()

          return
        }

        pkr.add(
          PokemonResolvable.Int(pokemonIndex)
        )
      }
    }

    if (releaseState == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.pokemon.commands.release.errors.notInRelease")
        ).build()
      ).queue()
      return
    }
    if(!releaseState.initiator.releaseTrade) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.pokemon.commands.release.errors.inTrade")
        ).build()
      ).queue()
      return
    }

    if (pkr.isEmpty()) {
      context.reply(context.embedTemplates.error(context.translate("misc.errors.pokemonNotFound")).build())
        .queue()
      return
    }

    val acceptedResPokemon = mutableListOf<OwnedPokemon>()
    pkr.map { pokemon ->
      val pokemonRes = context.resolvePokemon(context.author, userData, pokemon)

      val authorReleaseState = releaseState.initiator

      when {
        pokemonRes == null -> {
          context.reply(context.embedTemplates.error(context.translate("misc.errors.pokemonNotFound")).build())
            .queue()
          return
        }
        pokemonRes._id == userData.selected -> {
          context.reply(
            context.embedTemplates.error(context.translate("modules.pokemon.commands.release.errors.selectedPokemon"))
              .build()
          )
            .queue()
          return
        }
        pokemonRes.favorite -> {
          context.reply(
            context.embedTemplates.error(context.translate("modules.pokemon.commands.release.errors.favoritePokemon"))
              .build()
          )
            .queue()
          return
        }
        (authorReleaseState.pokemon.size + pkr.size) >= 50 -> {
          context.reply(
            context.embedTemplates.error(
              context.translate("modules.trading.commands.add.errors.maxPokemonCount")
            ).build()
          ).queue()
          return
        }
        else -> {
          acceptedResPokemon.add(pokemonRes)
        }
      }
    }

    val authorPokemonText =
      TradingModule.getTradeStatePokemonText(context, acceptedResPokemon, acceptedResPokemon.map { it.id }, false)

    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.pokemon.commands.release.embeds.confirmation.embedNoConfirm.description",
          mapOf(
            "pokemon" to authorPokemonText.joinToString("\n").ifEmpty { "None" }
          )
        ),
        context.translate("modules.pokemon.commands.release.embeds.confirmation.embedNoConfirm.title")
      ).build()
    ).queue()

    acceptedResPokemon.map { pokemonR ->
      val session = context.bot.database.startSession()
      session.use {
        session.startTransaction()
        context.bot.database.tradeRepository.addPokemon(releaseState, context.author.id, pokemonR._id, session)
        context.bot.database.tradeRepository.clearConfirmState(releaseState, session)
        session.commitTransactionAndAwait()
      }
    }
  }
}