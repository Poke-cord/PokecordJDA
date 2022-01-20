package xyz.pokecord.bot.modules.pokemon.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.PokemonResolvable

object SelectCommand : Command() {
  override val name = "Select"

  override var aliases = arrayOf("s")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(name = "pokemon", optional = true) pokemonResolvable: PokemonResolvable?
  ) {
    if (!context.hasStarted(true)) return

    val currentSelfBattle = context.bot.database.battleRepository.getUserCurrentBattle(context.author)
    if (currentSelfBattle != null) {
      context.reply(context.embedTemplates.error(context.translate("modules.pokemon.commands.select.errors.inBattle")).build())
        .queue()
      return
    }

    val userData = context.getUserData()
    val pokemon = context.resolvePokemon(context.author, userData, pokemonResolvable)
    if (pokemon == null) {
      context.reply(context.embedTemplates.error(context.translate("misc.errors.pokemonNotFound")).build())
        .queue()
      return
    } else if (pokemon._id == userData.selected) {
      context.reply(
        context.embedTemplates.error(context.translate("modules.pokemon.commands.select.errors.alreadySelected")).build()
      )
        .queue()
      return
    }

    module.bot.database.userRepository.selectPokemon(userData, pokemon)

    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.pokemon.commands.select.embed.description",
          mapOf(
            "pokemon" to context.translator.pokemonDisplayName(pokemon),
            "level" to pokemon.level.toString(),
            "ivPercentage" to pokemon.ivPercentage
          )
        ),
        context.translate("modules.pokemon.commands.select.embed.title")
      )
        .setColor(pokemon.data.species.color.colorCode)
        .build()
    ).queue()
  }
}
