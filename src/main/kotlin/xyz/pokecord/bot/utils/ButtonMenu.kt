package xyz.pokecord.bot.utils

import dev.minn.jda.ktx.await
import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.ButtonStyle
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.MessageCommandContext
import xyz.pokecord.bot.core.structures.discord.SlashCommandContext
import java.util.concurrent.Executors

abstract class ButtonMenu(
  private val context: ICommandContext,
  protected var buttons: List<ButtonData>,
  protected var embed: MessageEmbed,
  private val timeout: Int = 60_000,
  protected var sentMessage: Message? = null
) {
  private var endTime: Long? = null
  private val coroutineScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

  private var latestEvent: ButtonClickEvent? = null
  private var latestJob: Job? = null

  abstract suspend fun onButtonClick(button: ButtonData, context: ICommandContext, event: ButtonClickEvent)

  init {
    if (buttons.groupingBy { it.id }.eachCount().any { it.value > 1 }) {
      throw IllegalArgumentException("All buttons must have unique IDs.")
    }
  }

  fun start() {
    if (buttons.isEmpty()) return

    latestJob?.cancel()

    latestJob = coroutineScope.launch {
      val actionRows = buttons.map { Button.of(it.style, it.id, it.text, it.emoji) }.chunked(5).map { ActionRow.of(it) }

      if (latestEvent == null) {
        val result = context.addActionRows(*actionRows.toTypedArray()).reply(embed).await()
        context.clearActionRows()

        sentMessage = when (context) {
          is MessageCommandContext -> {
            result as Message
          }
          is SlashCommandContext -> {
            (result as InteractionHook).retrieveOriginal().await()
          }
          else -> throw IllegalStateException("Unknown command context type ${context::class.java.name}")
        }
      } else {
        stop()
        latestEvent!!.editMessageEmbeds(embed).setActionRows(actionRows).await()
      }

      if (sentMessage == null) return@launch
      endTime = System.currentTimeMillis() + timeout

      while (endTime != null) {
        val difference = endTime!! - System.currentTimeMillis()
        if (difference <= 0) {
          stop()
          break
        }

        val timeoutStatus = withTimeoutOrNull(difference) {
          val event = context.jda.await<ButtonClickEvent> {
            it.messageId == sentMessage!!.id
          }

          if (event.user.id != context.author.id) {
            event.deferEdit().queue()
          } else {
            latestEvent = event
            buttons.find { it.id == event.componentId }?.let { onButtonClick(it, context, event) }
          }
        }

        if (timeoutStatus == null) {
          stop()
          break
        }
      }
    }
  }

  private suspend fun stop() {
    sentMessage?.editMessageEmbeds(embed)?.setActionRows()?.await()
  }

  data class ButtonData(
    val id: String,
    val text: String?,
    val emoji: Emoji?,
    val style: ButtonStyle = ButtonStyle.PRIMARY,
  )
}
