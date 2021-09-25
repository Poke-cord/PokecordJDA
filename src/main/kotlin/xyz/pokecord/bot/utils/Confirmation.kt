package xyz.pokecord.bot.utils

import dev.minn.jda.ktx.await
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.MessageCommandContext
import xyz.pokecord.bot.core.structures.discord.SlashCommandContext

class Confirmation(private val context: ICommandContext, val authorId: String, val timeout: Long = 30_000) {
  private enum class ConfirmationOptions(val emoji: String, val id: String, val text: String) {
    CHECK("✅", "yes", "Yes"),
    CROSS("❎", "no", "No");

    companion object {
      val values = values()

      fun getByButtonId(buttonId: String): ConfirmationOptions? {
        return values.find { it.id == buttonId }
      }
    }
  }

  var sentMessage: Message? = null
  var timedOut = false

  suspend fun result(embedBuilder: EmbedBuilder): Boolean {
    context.bot.cache.setRunningCommand(authorId, true)

    val footer = embedBuilder.build().footer?.text
    if (footer != null) embedBuilder.setFooter(footer.replace("{{timeout}}", (timeout / 1000).toString()))

    val endTime = System.currentTimeMillis() + timeout

    // TODO: make sure it works for slash commands
    val replyActionResult = context.addActionRows(
      ActionRow.of(
        ConfirmationOptions.values.map {
          Button.secondary(it.id, Emoji.fromUnicode(it.emoji))
//          Button.secondary(it.id, it.text).withEmoji(Emoji.fromUnicode(it.emoji))
        }
      )).reply(embedBuilder.build()).await()

    context.clearActionRows()

    sentMessage = when (context) {
      is MessageCommandContext -> replyActionResult as Message
      is SlashCommandContext -> (replyActionResult as InteractionHook).retrieveOriginal().await()
      else -> throw IllegalStateException("Unknown command context type ${context::class.java.name}")
    }

    if (sentMessage == null) return false

    var result = false

    while (System.currentTimeMillis() < endTime) {
      val difference = endTime - System.currentTimeMillis()
      if (difference <= 0) {
        stop()
        break
      }

      var timeoutStatus = withTimeoutOrNull(difference) {
        val event = context.jda.await<ButtonClickEvent> {
          it.messageId == sentMessage!!.id
        }

        if (event.user.id != authorId) {
          event.replyEmbeds(
            context.embedTemplates.error(
              context.translate("misc.errors.notYourButton")
            ).build()
          )
            .setEphemeral(true)
            .queue()
        } else {
          context.bot.cache.setRunningCommand(authorId, false)
          sentMessage?.let {
            if (it.isFromGuild && it.channel is TextChannel && (it.channel as TextChannel).guild.selfMember.hasPermission(
                it.channel as TextChannel,
                Permission.MESSAGE_MANAGE
              )
            ) {
              it.clearReactions().queue()
            } else {
              ConfirmationOptions.values().reversed().forEach { emojiEnum ->
                it.removeReaction(emojiEnum.emoji).queue()
              }
            }
          }
          if (event.messageId == sentMessage!!.id && event.user.id == authorId) {
            val option = ConfirmationOptions.getByButtonId(
              event.componentId
            )

            if (option != null) {
              result = option == ConfirmationOptions.CHECK
              stop()
              null
            } else 0
          } else {
            0
          }
        }
      }
      if (timeoutStatus == null) {
        timeoutStatus = true
        stop()
        break
      }
    }

    return result
  }

  private fun stop() {
    sentMessage?.embeds?.first()?.let { sentMessage?.editMessageEmbeds(it)?.setActionRows()?.queue() }
  }
}
