package xyz.pokecord.bot.utils

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.MessageCommandContext
import xyz.pokecord.bot.core.structures.discord.SlashCommandContext

class Confirmation(private val context: ICommandContext, val timeout: Long = 30_000) {
  private enum class Emojis(val emoji: String) {
    CHECK("✅"),
    CROSS("❎");

    companion object {
      fun isValid(emoji: String) = values().map { it.emoji }.contains(emoji)
    }
  }

  var message: Message? = null
  var timedOut = false

  suspend fun result(embedBuilder: EmbedBuilder): Boolean {
    context.bot.cache.setRunningCommand(context.author.id, true)
    val footer = embedBuilder.build().footer?.text
    if (footer != null) embedBuilder.setFooter(footer.replace("{{timeout}}", (timeout / 1000).toString()))

    // TODO: make sure it works for slash commands
    val result = context.reply(embedBuilder.build()).await()
    message = when (context) {
      is MessageCommandContext -> result as Message
      is SlashCommandContext -> (result as InteractionHook).retrieveOriginal().await()
      else -> throw IllegalStateException("Unknown command context type ${context::class.java.name}")
    }
    Emojis.values().map { message!!.addReaction(it.emoji).queue() }
    val endTime = System.currentTimeMillis() + timeout
    val event = context.jda.await<GenericEvent> {
      if (endTime - System.currentTimeMillis() <= 0) {
        timedOut = true
        true
      } else {
        it is MessageReactionAddEvent && it.messageId == message!!.id && it.userId == context.author.id && Emojis.isValid(
          it.reactionEmote.emoji
        )
      }
    }
    context.bot.cache.setRunningCommand(context.author.id, false)
    message?.let {
      if (it.isFromGuild && it.channel is TextChannel && (it.channel as TextChannel).guild.selfMember.hasPermission(
          it.channel as TextChannel,
          Permission.MESSAGE_MANAGE
        )
      ) {
        it.clearReactions().queue()
      } else {
        Emojis.values().reversed().forEach { emojiEnum ->
          it.removeReaction(emojiEnum.emoji).queue()
        }
      }
    }
    return event is MessageReactionAddEvent && event.messageId == message!!.id && event.userId == context.author.id && event.reactionEmote.emoji == Emojis.CHECK.emoji
  }
}
