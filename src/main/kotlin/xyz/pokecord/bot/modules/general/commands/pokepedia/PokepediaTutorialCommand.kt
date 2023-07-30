package xyz.pokecord.bot.modules.general.commands.pokepedia

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object PokepediaTutorialCommand : Command() {
  override val name = "Tutorial"
  override var aliases = arrayOf("guide")

  @Executor
  suspend fun execute(
    context: ICommandContext
  ) {
    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.general.commands.pokepedia.tutorial.****",
          mapOf(
            "serverLink" to "https://discord.gg/EUYgq3Jub3",
            "inviteLink" to "https://pokecord.zihad.dev/invite",
            "websiteLink" to "https://pokecord.zihad.dev",
            "tosLink" to "https://sites.google.com/view/pokecord4908/english/terms"
          )
        ),
        context.translate("modules.general.commands.pokepedia.tutorial.****")
      ).setFooter(context.translate("modules.general.commands.pokepedia.tutorial.****"))
        .build()
    ).queue()
  }
}
