package xyz.pokecord.bot.modules.profile.commands.profile

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

//This command to eventually merge with @pkc profile.

object ProfileInfoCommand : Command() {
  override val name = "Info"

  override var aliases = arrayOf("i", "s", "stat", "stats")

  @Executor
  suspend fun execute(
    context: ICommandContext
  ) {
    val pokemonCount = module.bot.database.pokemonRepository.getPokemonCount(context.author.id)
    val shinyRate = module.bot.database.userRepository.getUser(context.author.id).shinyRate

    context.reply(
      context.embedTemplates.normal(
        context.translate("modules.profile.commands.profile.embed.info.description",
          mapOf(
//            "trainerId" to trainerId,
            "count" to pokemonCount.toString(),
            "rate" to shinyRate.toString()
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