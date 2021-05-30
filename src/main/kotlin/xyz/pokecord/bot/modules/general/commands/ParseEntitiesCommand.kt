package xyz.pokecord.bot.modules.general.commands

import net.dv8tion.jda.api.entities.*
import xyz.pokecord.bot.core.structures.discord.Command
import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext

class ParseEntitiesCommand : Command() {
  override val name = "Parse-Entities"

  override var aliases = arrayOf("pe")
  override var rateLimit = 5000L

  @Executor
  fun execute(
    context: MessageReceivedContext,
    @Argument(optional = true) user: User?,
    @Argument(optional = true) member: Member?,
    @Argument(optional = true) role: Role?,
    @Argument(optional = true) textChannel: TextChannel?,
    @Argument(optional = true) voiceChannel: VoiceChannel?
  ) {
    context.reply(
      context.embedTemplates.normal(
        """
      User:            ${user?.asMention ?: "N/A"}
      Member:          ${member?.asMention ?: "N/A"}
      Role:            ${role?.asMention ?: "N/A"}
      Text Channel:    ${textChannel?.asMention ?: "N/A"}
      Voice Channel:   ${voiceChannel?.name ?: "N/A"}
    """.trimIndent(), "Parsed Entities"
      ).build()
    ).queue()
  }
}
