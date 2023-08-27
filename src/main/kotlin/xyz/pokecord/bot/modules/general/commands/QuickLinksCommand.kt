package xyz.pokecord.bot.modules.general.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

class QuickLinksCommand : Command() {
  override val name = "QuickLinks"
  override var aliases = arrayOf("l", "link", "links", "ql", "quick link", "quick links", "inv", "donate", "support")

  @Executor
  suspend fun execute(
    context: ICommandContext
  ) {
//    context.embedTemplates.normal(
//
//    )
//    val embedAuthorText = context.translate(
//      "modules.general.commands.invite.embed.author.text",
//      mapOf("botUsername" to context.jda.selfUser.name)
//    )
//    val embedDescription = context.translate(
//      "modules.general.commands.invite.embed.description",
//     mapOf(
//      "botUsername" to context.jda.selfUser.name,
//        "inviteLink" to "https://pokecord.xyz/invite",
//      )
//    )
    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.general.commands.invite.embed.description",
          mapOf(
            "serverLink" to "https://discord.gg/EUYgq3Jub3",
            "inviteLink" to "https://pokecord.zihad.dev/invite",
            "websiteLink" to "https://pokecord.zihad.dev",
            "tosLink" to "https://sites.google.com/view/pokecord4908/english/terms"
          )
        ),
        context.translate("modules.general.commands.invite.embed.title")
      ).setFooter(context.translate("modules.general.commands.invite.embed.footer"))
        .build()
    ).queue()
  }
}
