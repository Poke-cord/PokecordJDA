package xyz.pokecord.bot.modules.general.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

class LeaderboardCommand : Command() {
  override val name = "Leaderboard"

  override var aliases = arrayOf("lb")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(aliases = ["c"], prefixed = true, optional = true) credits: Boolean?
  ) {
    val selfUserId = context.jda.selfUser.id
    if (credits == true) {
      val entries = module.bot.database.userRepository.getCreditLeaderboard(selfUserId)
      context.reply(
        context.embedTemplates.normal(
          entries.mapIndexed { i, it ->
            "${i + 1}. ${it.tag?.dropLast(5) ?: "N/A"} - ${context.translator.numberFormat(it.credits)}"
          }.joinToString("\n"),
          "Global Pokéboard by Credit Amount"
        )
          .build()
      ).queue()
    } else {
      val entries = module.bot.database.userRepository.getPokemonCountLeaderboard(selfUserId)
      context.reply(
        context.embedTemplates.normal(
          entries.mapIndexed { i, it ->
            "${i + 1}. ${it.tag.dropLast(5)} - ${it.pokemonCount}"
          }.joinToString("\n"),
          "Global Pokéboard by Pokémon Count"
        )
          .build()
      ).queue()
    }
  }
}
