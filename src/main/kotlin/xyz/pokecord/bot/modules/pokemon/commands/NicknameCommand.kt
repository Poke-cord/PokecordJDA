package xyz.pokecord.bot.modules.pokemon.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.utils.Config
import xyz.pokecord.bot.utils.PokemonResolvable

class NicknameCommand : Command() {
  override val name = "Nickname"

  override var aliases = arrayOf("n")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(name = "pokemon", optional = true) pokemonResolvable: PokemonResolvable?,
    @Argument(consumeRest = true, optional = true) nickname: String?
  ) {
    if (!context.hasStarted(true)) return

    val pokemon = context.resolvePokemon(context.author, context.getUserData(), pokemonResolvable)
    if (pokemon == null) {
      context.reply(context.embedTemplates.error(context.translate("misc.errors.pokemonNotFound")).build())
        .queue()
      return
    }

    if (!nickname.isNullOrBlank()) {
      if (nickname.length > 64) {
        context.reply(
          context.embedTemplates.error(context.translate("modules.pokemon.commands.nickname.errors.charLimit")).build()
        ).queue()
        return
      } else if (nickname.endsWith(Config.Emojis.EVENT_SHINY) || nickname.endsWith(Config.Emojis.EVENT) || nickname.endsWith(
          Config.Emojis.SHINY
        )
      ) {
        context.reply(
          context.embedTemplates.error(context.translate("modules.pokemon.commands.nickname.errors.emojiNotAllowed"))
            .build()
        ).queue()
        return
      } else {
        val otherPokemon = Pokemon.getByName(nickname.trim())
        if (otherPokemon != null) {
          context.reply(
            context.embedTemplates.error(context.translate("modules.pokemon.commands.nickname.errors.pokemonName"))
              .build()
          ).queue()
          return
        }
      }
    }

    module.bot.database.pokemonRepository.setNickname(pokemon, nickname)

    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.pokemon.commands.nickname.embed.description",
          mapOf(
            "pokemon" to context.translator.pokemonName(pokemon)!!,
            "nickname" to (pokemon.nickname ?: "None"),
            "level" to pokemon.level.toString(),
            "ivPercentage" to pokemon.ivPercentage
          )
        ),
        context.translate(if (!nickname.isNullOrBlank()) "modules.pokemon.commands.nickname.updated" else "modules.pokemon.commands.nickname.reset")
      )
        .setColor(pokemon.data.species.color.colorCode)
        .build()
    ).queue()
  }
}
