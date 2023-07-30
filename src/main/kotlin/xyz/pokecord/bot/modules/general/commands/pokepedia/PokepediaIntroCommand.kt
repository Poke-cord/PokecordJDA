package xyz.pokecord.bot.modules.general.commands.pokepedia

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object PokepediaIntroCommand : Command() {
  override val name = "Intro"
  override var aliases = arrayOf("introduction")

  @Executor
  suspend fun execute(
    context: ICommandContext
  ) {
    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.general.commands.pokepedia.intro.description",
          mapOf(
            "user" to context.author.asMention
          )
        ),
        context.translate("modules.general.commands.pokepedia.intro.title")
      ).setFooter(context.translate("modules.general.commands.pokepedia.intro.footer"))
        .build()
    ).queue()
  }
}
