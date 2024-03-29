package xyz.pokecord.bot.core.structures.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import xyz.pokecord.bot.core.structures.discord.base.BaseCommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.discord.base.Event
import xyz.pokecord.bot.core.structures.discord.base.Module
import java.time.OffsetDateTime
import javax.annotation.CheckReturnValue
import net.dv8tion.jda.api.entities.Guild as JDAGuild
import net.dv8tion.jda.api.entities.User as JDAUser

class MessageCommandContext(bot: Bot, override val event: MessageReceivedEvent) : BaseCommandContext(bot) {
  override val author: JDAUser
    get() = event.author
  override val channel: MessageChannel
    get() = event.channel
  override val guild: JDAGuild?
    get() = if (event.isFromGuild) event.guild else null
  override val isFromGuild: Boolean
    get() = event.isFromGuild
  override val jda: JDA
    get() = event.jda
  override val timeCreated: OffsetDateTime
    get() = event.message.timeCreated

  private val actionRows = mutableListOf<ActionRow>()
  private val attachments = mutableListOf<Pair<ByteArray, String>>()

  override fun addActionRows(vararg actionRows: ActionRow) = this.also { this.actionRows.addAll(actionRows) }
  override fun clearActionRows() = this.also { actionRows.clear() }

  override fun addAttachment(data: ByteArray, name: String) = this.also { attachments.add(Pair(data, name)) }

  @Suppress("CheckReturnValue")
  @CheckReturnValue
  override fun reply(content: String, mentionRepliedUser: Boolean) =
    event.message.reply(content).mentionRepliedUser(mentionRepliedUser).setActionRows(actionRows).also {
      attachments.forEach { attachment ->
        it.addFile(attachment.first, attachment.second)
      }
    }

  @Suppress("CheckReturnValue")
  @CheckReturnValue
  override fun reply(embed: MessageEmbed, mentionRepliedUser: Boolean) =
    event.message.replyEmbeds(embed).mentionRepliedUser(mentionRepliedUser).setActionRows(actionRows).also {
      attachments.forEach { attachment ->
        it.addFile(attachment.first, attachment.second)
      }
    }

  override suspend fun handleException(
    exception: Throwable,
    module: Module?,
    command: Command?,
    event: Event?,
    extras: Map<String, String>
  ) {
    super.handleException(
      exception,
      module,
      command,
      event,
      mapOf(
        "messageContent" to this.event.message.contentRaw,
        "messageId" to this.event.message.id,
        "channelId" to this.event.channel.id,
      )
    )
  }

  override fun hasMention(id: String): Boolean {
    return event.message.getMentions().any { it.id == id }
  }
}
