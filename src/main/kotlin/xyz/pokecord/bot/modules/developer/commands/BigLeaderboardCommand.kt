package xyz.pokecord.bot.modules.developer.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.modules.developer.DeveloperCommand

class BigLeaderboardCommand : DeveloperCommand() {
  override val name = "BigLeaderboard"

  override var aliases = arrayOf("blb")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(aliases = ["c"], prefixed = true, optional = true) credits: Boolean?
  ) {
    val selfUserId = context.jda.selfUser.id
    if (credits == true) {
      val entries = module.bot.database.userRepository.getCreditLeaderboard(selfUserId, 60)
      context.reply(
        context.embedTemplates.normal(
          entries.mapIndexed { i, it ->
            "${i + 1}. ${it.tag?.dropLast(0) ?: "N/A"} - ${context.translator.numberFormat(it.credits)}"
          }.joinToString("\n"),
          context.translate("modules.general.commands.leaderboard.titles.credits")
        ).setFooter(context.translate("modules.general.commands.leaderboard.footer"))
          .build()
      ).queue()
    } else {
      val entries = module.bot.database.userRepository.getPokemonCountLeaderboard(selfUserId, 60)
      context.reply(
        context.embedTemplates.normal(
          entries.mapIndexed { i, it ->
            "${i + 1}. ${it.tag.dropLast(0)} - ${it.pokemonCount}"
          }.joinToString("\n"),
          context.translate("modules.general.commands.leaderboard.titles.pokemon")
        ).setFooter(context.translate("modules.general.commands.leaderboard.footer"))
          .build()
      ).queue()
    }
  }
}
