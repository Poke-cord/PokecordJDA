package xyz.pokecord.bot.core.structures.discord

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import xyz.pokecord.bot.core.structures.discord.base.BaseCommandContext

class EmbedTemplates(val context: BaseCommandContext) {
  enum class Color(val code: Int) {
    GREEN(0x2ecc71),
    RED(0xf04747),
    YELLOW(0xf0e365)
  }

  fun empty() = EmbedBuilder().setColor(Color.YELLOW.code)

  suspend fun error(description: String, title: String? = null): EmbedBuilder {
    val embedTitle = title ?: context.translate("misc.embeds.error.title")
    return EmbedBuilder().setColor(Color.RED.code).setTitle(embedTitle).setDescription(description)
      .setFooter(context.translate("misc.embeds.error.footer"))
  }

  fun confirmation(description: String, title: String? = null) =
    EmbedBuilder().setColor(0xfaa61a).setDescription(description).setTitle(
      title
    )

  fun normal(description: String, title: String? = null): EmbedBuilder {
    return empty().setDescription(description).setTitle(title)
  }

  suspend fun progressPrivate(user: User) = error(
    context.translate("misc.embeds.progressPrivate.description", "user" to user.asMention),
    context.translate("misc.embeds.progressPrivate.title")
  )

  suspend fun start() = normal(
    context.translate("misc.checks.hasStarted.embed.description", "prefix" to context.getPrefix()),
    context.translate("misc.checks.hasStarted.embed.title")
  )
}
