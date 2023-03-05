package xyz.pokecord.bot.modules.pokemon.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.PokemonResolvable

class FavoriteCommand : Command() {
  override val name = "Favorite"

  override var aliases = arrayOf("f", "fav", "favourite")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(name = "pokemon", optional = true) pokemonResolvable: PokemonResolvable?
  ) {
    if (!context.hasStarted(true)) return

    var pokemon = context.resolvePokemon(context.author, context.getUserData(), pokemonResolvable)
    if (pokemon == null) {
      context.reply(context.embedTemplates.error(context.translate("misc.errors.pokemonNotFound")).build())
        .queue()
      return
    }

    pokemon = module.bot.database.pokemonRepository.toggleFavoriteStatus(pokemon)
    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.pokemon.commands.favorite.embed.description",
            "pokemon" to context.translator.pokemonDisplayName(pokemon)
        ),
        context.translate(if (pokemon.favorite) "modules.pokemon.commands.favorite.added" else "modules.pokemon.commands.favorite.removed")
      )
        .setColor(pokemon.data.species.color.colorCode)
        .build()
    ).queue()
  }
}
