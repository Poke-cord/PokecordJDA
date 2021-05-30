package xyz.pokecord.bot.modules.general.commands

import xyz.pokecord.bot.core.structures.discord.Command
import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext

class LeaderboardCommand : Command() {
  override val name = "Leaderboard"

  override var aliases = arrayOf("lb")

  @Executor
  suspend fun execute(
    context: MessageReceivedContext,
    @Argument(aliases = ["c"], prefixed = true, optional = true) credits: Boolean?
  ) {
    if (credits == true) {
      val entries = module.bot.database.userRepository.getCreditLeaderboard()
      context.reply(
        context.embedTemplates.normal(
          entries.mapIndexed { i, it ->
            "${i + 1}. ${it.tag?.dropLast(5) ?: "N/A"} - ${context.translator.numberFormat(it.credits)}"
          }.joinToString("\n"),
          "Global Leaderboard by Amount of Credits"
        )
          .build()
      ).queue()
    } else {
      val entries = module.bot.database.userRepository.getPokemonCountLeaderboard()
      context.reply(
        context.embedTemplates.normal(
          entries.mapIndexed { i, it ->
            "${i + 1}. ${it.tag.dropLast(5)} - ${it.pokemonCount}"
          }.joinToString("\n"),
          "Global Leaderboard by Number of Pokémon"
        )
          .build()
      ).queue()
    }
  }
}
