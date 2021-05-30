package xyz.pokecord.bot.modules.pokemon.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import xyz.pokecord.bot.core.managers.database.repositories.PokemonRepository
import xyz.pokecord.bot.core.structures.discord.Command
import xyz.pokecord.bot.core.structures.discord.EmbedTemplates
import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext
import xyz.pokecord.bot.utils.EmbedPaginator
import kotlin.math.ceil

class PokemonCommand : Command() {
  override val name = "Pokemon"
  override var aliases = arrayOf("p")

  @Executor
  suspend fun execute(
    context: MessageReceivedContext,
    @Argument(aliases = ["sh"], prefixed = true, optional = true) shiny: Boolean?,
    @Argument(aliases = ["fav", "favs", "f"], prefixed = true, optional = true) favorites: Boolean?,
    @Argument(aliases = ["o", "sort"], prefixed = true, optional = true) order: String?,
    @Argument(aliases = ["n"], prefixed = true, optional = true) nature: String?,
    @Argument(aliases = ["r"], prefixed = true, optional = true) rarity: String?,
    @Argument(aliases = ["t"], prefixed = true, optional = true) type: String?,
    @Argument(aliases = ["re"], prefixed = true, optional = true) regex: Regex?,
    @Argument(optional = true) page: Int?,
    @Argument(optional = true) user: User?,
    @Argument(
      aliases = ["s"],
      name = "search",
      consumeRest = true,
      prefixed = true,
      optional = true
    ) searchQuery: String?
  ) {
    val targetUser = user ?: context.author
    val checkingSelf = user == null

    val userData = if (checkingSelf) context.getUserData() else module.bot.database.userRepository.getUser(targetUser)
    // TODO: moderator check
    if (userData.progressPrivate && !checkingSelf) {
      context.reply(context.embedTemplates.progressPrivate(targetUser).build()).queue()
      return
    }

    val count = module.bot.database.pokemonRepository.getPokemonCount(
      targetUser.id,
      PokemonRepository.PokemonSearchOptions(order, favorites, nature, rarity, shiny, type, regex, searchQuery)
    )
    val templateEmbedBuilder =
      EmbedBuilder()
        .setTitle(
          context.translate(
            "modules.pokemon.commands.pokemon.embeds.title",
            "user" to targetUser.asTag
          )
        )
        .setColor(EmbedTemplates.Color.GREEN.code)
    if (count < 1) {
      context.reply(
        templateEmbedBuilder.setDescription(context.translate("modules.pokemon.commands.pokemon.errors.noResults"))
          .setColor(EmbedTemplates.Color.RED.code).build()
      ).queue()
      return
    }
    val pageCount = ceil((count.toDouble() / 15)).toInt()
    val paginator = EmbedPaginator(context, pageCount, { pageIndex ->
      if (pageIndex >= pageCount) {
        this.stop()
        return@EmbedPaginator templateEmbedBuilder.setDescription(context.translate("modules.pokemon.commands.pokemon.errors.noResults"))
          .setColor(EmbedTemplates.Color.RED.code).setFooter("")
      }
      val ownedPokemonList = module.bot.database.pokemonRepository.getPokemonList(
        targetUser.id,
        searchOptions = PokemonRepository.PokemonSearchOptions(
          order,
          favorites,
          nature,
          rarity,
          shiny,
          type,
          regex,
          searchQuery
        ),
        skip = pageIndex * 15
      )
      templateEmbedBuilder.clearFields().setFooter(null)
      for (pokemon in ownedPokemonList) {
        templateEmbedBuilder.addField(
          "${pokemon.index + 1} | ${context.translator.pokemonDisplayName(pokemon)}",
          "LVL **${pokemon.level}**, IV **${pokemon.ivPercentage}**", true
        )
      }
      templateEmbedBuilder
    }, if (page == null) 0 else page - 1)
    paginator.start()
  }
}
