package xyz.pokecord.bot.modules.trainer.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.core.structures.pokemon.Species

class StartCommand : Command() {
  override val name = "Start"

  private val groupedPokemon: Map<String, List<Species>>

  init {
    val pokemon = Pokemon.starters.mapNotNull { id ->
      return@mapNotNull Pokemon.getById(id)?.species
    }
    groupedPokemon = pokemon.groupBy {
      it.romanGenerationId
    }
  }

  @Executor
  suspend fun execute(context: ICommandContext) {
    if (context.hasStarted()) {
      context.reply(
        context.embedTemplates.error(context.translate("modules.pokemon.commands.start.startingTwice")).build()
      ).queue()
    } else {
      context.reply(
        context.embedTemplates.normal(
          context.translate("modules.pokemon.commands.start.description", "prefix" to context.getPrefix()),
          context.translate("modules.pokemon.commands.start.welcomeToPokeWorld")
        ).apply {
          val generation = context.translate("misc.texts.generation")
          groupedPokemon.forEach {
            val names = it.value.mapNotNull { pokemon -> context.translator.pokemonName(pokemon) }
            addField("**$generation ${it.key}**", names.joinToString(" | "), false)
          }
          setImage("https://cdn.discordapp.com/attachments/771568547158949948/979295636555960340/unknown.png")
        }.build()
      ).queue()
    }
  }
}
