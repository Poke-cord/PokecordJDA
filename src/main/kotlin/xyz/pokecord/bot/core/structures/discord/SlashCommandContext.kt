package xyz.pokecord.bot.core.structures.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction
import xyz.pokecord.bot.core.structures.discord.base.BaseCommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.discord.base.Event
import xyz.pokecord.bot.core.structures.discord.base.Module
import java.time.OffsetDateTime

class SlashCommandContext(bot: Bot, override val event: SlashCommandEvent) : BaseCommandContext(bot) {
  override val author: User
    get() = event.user
  override val channel: MessageChannel
    get() = event.channel
  override val guild: Guild?
    get() = event.guild
  override val isFromGuild: Boolean
    get() = event.isFromGuild
  override val jda: JDA
    get() = event.jda
  override val timeCreated: OffsetDateTime
    get() = event.timeCreated

  private var replyDeferred = false

  private val actionRows = mutableListOf<ActionRow>()
  private val attachments = mutableListOf<Pair<ByteArray, String>>()

  fun deferReply(): ReplyAction {
    replyDeferred = true
    return event.deferReply()
  }

  override fun addActionRows(vararg actionRows: ActionRow) = this.also { this.actionRows.addAll(actionRows) }
  override fun clearActionRows() = this.also { actionRows.clear() }

  override fun addAttachment(data: ByteArray, name: String) = this.also { this.attachments.add(Pair(data, name)) }

  override fun reply(content: String, mentionRepliedUser: Boolean): RestAction<*> {
    return when {
      replyDeferred -> event.hook.editOriginal(content).setActionRows(actionRows).also {
        attachments.forEach { attachment -> it.addFile(attachment.first, attachment.second) }
      }
      else -> event.reply(content).mentionRepliedUser(mentionRepliedUser).addActionRows(actionRows).also {
        attachments.forEach { attachment -> it.addFile(attachment.first, attachment.second) }
      }
    }
  }

  override fun reply(embed: MessageEmbed, mentionRepliedUser: Boolean): RestAction<*> {
    return when {
      replyDeferred -> event.hook.editOriginalEmbeds(embed).setActionRows(actionRows).also {
        attachments.forEach { attachment -> it.addFile(attachment.first, attachment.second) }
      }
      else -> event.replyEmbeds(embed).mentionRepliedUser(mentionRepliedUser).addActionRows(actionRows).also {
        attachments.forEach { attachment -> it.addFile(attachment.first, attachment.second) }
      }
    }
  }

  override suspend fun handleException(
    exception: Throwable,
    module: Module?,
    command: Command?,
    event: Event?,
    extras: Map<String, String>
  ) {
    val tmpExtras = mutableMapOf(
      "channelId" to this.event.channel.id,

      "slashCommand/commandId" to this.event.commandId,
      "slashCommand/commandPath" to this.event.commandPath,
      "slashCommand/interactionId" to this.event.interaction.id,
      "slashCommand/options" to this.event.options.joinToString(", ") { it.toString() },
      "slashCommand/token" to this.event.token,
    )

    this.event.subcommandGroup?.let {
      tmpExtras["slashCommand/subCommandGroup"] = it
    }
    this.event.subcommandName?.let {
      tmpExtras["slashCommand/subCommandName"] = it
    }

    super.handleException(
      exception,
      module,
      command,
      event,
      tmpExtras
    )
  }

  override fun hasMention(id: String): Boolean {
    return event.options.any { it.type == OptionType.MENTIONABLE && it.asMentionable.id == id }
  }
}
