package xyz.pokecord.bot.modules.general.commands

import xyz.pokecord.bot.core.structures.discord.Command
import xyz.pokecord.bot.core.structures.discord.EmbedTemplates
import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext

class InviteCommand : Command() {
  override val name = "Invite"

  override var aliases = arrayOf("inv")

  @Executor
  suspend fun execute(
    context: MessageReceivedContext
  ) {
    val embedAuthorText = context.translate(
      "modules.general.commands.invite.embed.author.text",
      mapOf("botUsername" to module.bot.jda.selfUser.name)
    )
    val embedDescription = context.translate(
      "modules.general.commands.invite.embed.description",
      mapOf(
        "botUsername" to context.jda.selfUser.name,
        "inviteLink" to "https://pokecord.xyz/invite",
      )
    )
    context.reply(
      context.embedTemplates.normal(embedDescription)
        .setColor(EmbedTemplates.Color.GREEN.code)
        .setAuthor(embedAuthorText, module.bot.jda.selfUser.effectiveAvatarUrl)
        .build()
    ).queue()
  }
}
