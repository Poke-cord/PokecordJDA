package xyz.pokecord.bot.utils

import dev.minn.jda.ktx.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.MessageCommandContext
import xyz.pokecord.bot.core.structures.discord.SlashCommandContext
import java.util.concurrent.Executors

class EmbedPaginator(
  private val context: ICommandContext,
  private val pageCount: Int,
  private val pageExtractor: suspend EmbedPaginator.(pageIndex: Int) -> EmbedBuilder,
  initialPageIndex: Int = 0,
  private val shouldSetFooter: Boolean = true,
  private val timeout: Int = 60_000
) {
  private enum class NavigationOptions(val emoji: String, val buttonId: String, val buttonText: String) {
    First("⏮️", "first", "First"),
    Prev("◀️", "prev", "Prev"),

    //    Stop("⏹️", "stop", "Stop"),
    Next("▶️", "next", "Next"),
    Last("⏭️", "last", "Last");


    companion object {
      val values = values()

      fun getByButtonId(buttonId: String): NavigationOptions? {
        return values.find { it.buttonId == buttonId }
      }
    }
  }

  private var sentMessage: Message? = null
  private var currentPageIndex: Int = initialPageIndex
  private var endTime: Long? = null
  private val coroutineScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

  private suspend fun getEmbed(pageIndex: Int = currentPageIndex): MessageEmbed {
    val embedBuilder = pageExtractor(pageIndex)
    if (pageCount > 1) {
      val footer = embedBuilder.build().footer
      if (footer != null) {
        embedBuilder.setFooter(
          footer.text
            ?.replace("{{page}}", (pageIndex + 1).toString())
            ?.replace("{{totalPage}}", pageCount.toString()),
          footer.iconUrl
        )
      } else if (shouldSetFooter) embedBuilder.setFooter("Page ${pageIndex + 1} of $pageCount")
    }
    return embedBuilder.build()
  }

  suspend fun start() {
    if (pageCount == 1) {
      context.reply(getEmbed()).queue()
      return
    }

    coroutineScope.launch {
      val result = context.addActionRows(
        ActionRow.of(
          NavigationOptions.values.map {
            Button.secondary(it.buttonId, it.buttonText).withEmoji(Emoji.fromUnicode(it.emoji))
          }
        )).reply(getEmbed()).await()
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

      if (sentMessage == null || pageCount == 1) return@launch
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
            event.deferReply()
          } else {
            var newPageIndex = when (NavigationOptions.getByButtonId(event.componentId)) {
              NavigationOptions.First -> 0
              NavigationOptions.Prev -> if (currentPageIndex == 0) pageCount - 1 else currentPageIndex - 1
//            NavigationOptions.Stop -> {
//              stop()
//              currentPageIndex
//            }
              NavigationOptions.Next -> if (currentPageIndex == pageCount - 1) 0 else currentPageIndex + 1
              NavigationOptions.Last -> pageCount - 1
              else -> currentPageIndex
            }
            if (currentPageIndex != newPageIndex) {
              if (newPageIndex > pageCount) newPageIndex = 0
              if (newPageIndex < 0) newPageIndex = pageCount - 1

              currentPageIndex = newPageIndex

              event.editMessageEmbeds(getEmbed()).await()
            } else {
              0 // return 0 lol
            }
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
    sentMessage?.editMessage(getEmbed())?.setActionRows()?.queue()
  }
}
