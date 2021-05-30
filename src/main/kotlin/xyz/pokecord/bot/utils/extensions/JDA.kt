package xyz.pokecord.bot.utils.extensions

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageReaction
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent

suspend inline fun Message.awaitReaction(
  user: User? = null, // filter by user
  crossinline filter: (MessageReaction) -> Boolean = { true } // filter by filter (lol)
): MessageReaction {
  return jda.await<MessageReactionAddEvent> {
    it.messageId == id
        && (user == null || it.user == user)
        && filter(it.reaction)
  }.reaction
}
