package xyz.pokecord.bot.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.EventListener
import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext
import xyz.pokecord.bot.utils.extensions.awaitSuspending

class EmbedPaginator(
  val context: MessageReceivedContext,
  private val pageCount: Int,
  val pageExtractor: suspend EmbedPaginator.(pageIndex: Int) -> EmbedBuilder,
  initialPageIndex: Int = 0,
  private val shouldSetFooter: Boolean = true,
  private val timeout: Int = 60_000
) {
  private enum class NavigationEmoji(val emoji: String) {
    First("⏮️"),
    Prev("◀️"),
    Stop("⏹️"),
    Next("▶️"),
    Last("⏭️")
  }

  private val navigationEmojis = NavigationEmoji.values()

  private var sentMessage: Message? = null
  private var currentPageIndex: Int = initialPageIndex
  private var endTime: Long? = null

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

  fun start(channel: MessageChannel = context.channel) {
    if (!channel.jda.eventManager.registeredListeners.contains(Companion)) {
      channel.jda.addEventListener(Companion)
    }
    GlobalScope.launch(Dispatchers.IO) {
      sentMessage = context.reply(getEmbed()).submit().awaitSuspending()
      if (sentMessage == null || pageCount == 1) return@launch
      navigationEmojis.forEach {
        sentMessage?.addReaction(it.emoji)?.queue()
      }
      endTime = System.currentTimeMillis() + timeout
      withContext(Dispatchers.Default) {
        while (endTime != null) {
          val difference = endTime!! - System.currentTimeMillis()
          if (difference <= 0) {
            stop()
            break
          } else {
            val event = eventChannel.receive()
            if (event !is MessageReactionAddEvent) continue
            if (event.user == null) continue
            if (event.messageId != sentMessage?.id || event.userId != context.author.id) continue
            GlobalScope.launch(Dispatchers.IO) {
              val newPageIndex = when (event.reactionEmote.emoji) {
                NavigationEmoji.First.emoji -> 0
                NavigationEmoji.Prev.emoji -> if (currentPageIndex == 0) pageCount - 1 else currentPageIndex - 1
                NavigationEmoji.Stop.emoji -> {
                  stop()
                  currentPageIndex
                }
                NavigationEmoji.Next.emoji -> if (currentPageIndex == pageCount - 1) 0 else currentPageIndex + 1
                NavigationEmoji.Last.emoji -> pageCount - 1
                else -> currentPageIndex
              }
              if (currentPageIndex != newPageIndex) {
                currentPageIndex = newPageIndex
                sentMessage?.editMessage(getEmbed())?.submit()?.awaitSuspending()
                if (event.channel is TextChannel && event.guild.selfMember.hasPermission(
                    event.channel as TextChannel,
                    Permission.MESSAGE_MANAGE
                  )
                ) {
                  event.reaction.removeReaction(event.user!!).queue()
                }
              }
            }
          }
        }
      }
    }
  }

  fun stop() {
    if (sentMessage != null) {
      val channel = sentMessage!!.channel
      if (context.isFromGuild && channel is TextChannel && channel.guild.selfMember.hasPermission(
          channel,
          Permission.MESSAGE_MANAGE
        )
      ) {
        sentMessage?.clearReactions()?.queue()
      } else {
        navigationEmojis.reversed().forEach {
          sentMessage?.removeReaction(it.emoji)?.queue()
        }
      }
    }
    endTime = null
  }

  companion object : EventListener {
    private val eventChannel = Channel<GenericEvent>()

    override fun onEvent(event: GenericEvent) {
      GlobalScope.launch {
        eventChannel.send(event)
      }
    }
  }
}
