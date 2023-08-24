package xyz.pokecord.bot.modules.profile.commands.profile

import net.dv8tion.jda.api.EmbedBuilder
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.EmbedTemplates
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.core.managers.database.repositories.PokemonRepository
import xyz.pokecord.bot.utils.Config
import xyz.pokecord.bot.utils.EmbedPaginator

//This command to eventually merge with @pkc profile.

object ProfileInfoCommand : Command() {
  override val name = "Info"

  override var aliases = arrayOf("i", "stat", "stats")

  @Executor
  suspend fun execute(
    context: ICommandContext
  ) {
    val pokemonCount = module.bot.database.pokemonRepository.getPokemonCount(context.author.id)
    context.reply(
      context.embedTemplates.normal(
        context.translate("modules.profile.commands.profile.embed.info.description",
          mapOf(
            "count" to pokemonCount.toString()
          )
        ),
        context.translate("modules.profile.commands.profile.embed.info.title",
          mapOf(
            "user" to context.author.asTag
          )
        )
      ).setFooter(context.translate("modules.profile.commands.profile.embed.info.footer"))
        .build()
    ).queue()
  }
}