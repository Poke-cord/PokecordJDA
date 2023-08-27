package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.api.ICommandContext

object GlimmeringCandyItem : Item(10010000, false) {
  const val categoryId = 1001

  override suspend fun use(context: ICommandContext, args: List<String>): UsageResult {
    val userData = context.getUserData()
    val shinyRate = context.bot.database.userRepository.getUser(context.author).shinyRate

    context.bot.database.userRepository.incShinyRate(userData, -10)
    return UsageResult(
      true,
      context.embedTemplates.normal(
        context.translate("items.glimmeringCandy.embed.description",
          mapOf(
            "rate" to shinyRate.toString(),
            "user" to context.author.asMention
          )
        ),
        context.translate("items.glimmeringCandy.embed.title")
      )
    )
  }
}
