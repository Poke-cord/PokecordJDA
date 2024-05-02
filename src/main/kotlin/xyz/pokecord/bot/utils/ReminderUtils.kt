package xyz.pokecord.bot.utils

import dev.minn.jda.ktx.await
import xyz.pokecord.App.bot
import xyz.pokecord.bot.core.managers.database.models.User
import xyz.pokecord.bot.core.structures.discord.EmbedTemplates
import java.util.concurrent.TimeUnit

object ReminderUtils {
  suspend fun sendVoteReminder(user: User) {
    if (!user.voteReminder) return

    val embedTemplates = EmbedTemplates()
    val timeRemaining = TimeUnit.HOURS.toMillis(12)

    try {
      val voterChannel = bot.shardManager
        .retrieveUserById(user.id).await()
        .openPrivateChannel().await()

      voterChannel.sendMessageEmbeds(
        embedTemplates.normal(
          embedTemplates.translate("modules.profile.commands.remind.dm.vote.description",
            "voteLink" to "https://top.gg/bot/705016654341472327/vote"
          ),
          embedTemplates.translate("modules.profile.commands.remind.dm.vote.title")
        ).setFooter("modules.profile.commands.remind.dm.vote.footer").build()
      ).queueAfter(timeRemaining, TimeUnit.MILLISECONDS)
    } catch (_: Exception) {
    }
  }
}
