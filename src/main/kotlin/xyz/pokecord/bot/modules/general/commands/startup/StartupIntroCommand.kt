package xyz.pokecord.bot.modules.general.commands.startup

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.EmbedTemplates
import xyz.pokecord.bot.core.structures.discord.base.Command

class StartupIntroCommand : Command() {
  override val name = "Intro"
  override var aliases = arrayOf("introduction")

  @Executor
  suspend fun execute(
    context: ICommandContext
  ) {
    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.general.commands.startup.description",
          mapOf(
            "serverLink" to "https://discord.gg/EUYgq3Jub3",
            "inviteLink" to "https://pokecord.zihad.dev/invite",
            "websiteLink" to "https://pokecord.zihad.dev",
            "tosLink" to "https://sites.google.com/view/pokecord4908/english/terms"
          )
        ),
        context.translate("modules.general.commands.startup.title")
      ).setFooter(context.translate("modules.general.commands.startup.footer"))
        .build()
    ).queue()
  }
}
