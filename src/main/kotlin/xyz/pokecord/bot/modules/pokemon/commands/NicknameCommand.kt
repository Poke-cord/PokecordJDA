package xyz.pokecord.bot.modules.pokemon.commands

import xyz.pokecord.bot.core.structures.discord.Command
import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.utils.PokemonResolvable

class NicknameCommand : Command() {
  override val name = "Nickname"

  override var aliases = arrayOf("n")

  @Executor
  suspend fun execute(
    context: MessageReceivedContext,
    @Argument(name = "pokemon", optional = true) pokemonResolvable: PokemonResolvable?,
    @Argument(consumeRest = true, optional = true) nickname: String?
  ) {
    if (!context.hasStarted(true)) return

    var pokemon = context.resolvePokemon(context.author, context.getUserData(), pokemonResolvable)
    if (pokemon == null) {
      context.reply(context.embedTemplates.error(context.translate("misc.errors.pokemonNotFound")).build())
        .queue()
      return
    }

    if (nickname != null) {
      if (nickname.length > 64) {
        context.reply(
          context.embedTemplates.error(context.translate("modules.pokemon.commands.nickname.errors.tooLong")).build()
        ).queue()
        return
      } else if (nickname.endsWith("⭐")) {
        context.reply(
          context.embedTemplates.error(context.translate("modules.pokemon.commands.nickname.errors.shinyStar")).build()
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

    pokemon = module.bot.database.pokemonRepository.setNickname(pokemon, nickname)

    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.pokemon.commands.nickname.embed.description",
          mapOf(
            "pokemon" to context.translator.pokemonName(pokemon)!!,
            "nickname" to (pokemon.nickname ?: ""),
            "level" to pokemon.level.toString(),
            "ivPercentage" to pokemon.ivPercentage
          )
        ),
        context.translate(if (pokemon.nickname != null) "modules.pokemon.commands.nickname.updated" else "modules.pokemon.commands.nickname.reset")
      )
        .setColor(pokemon.data.species.color.colorCode)
        .build()
    ).queue()
  }
}
