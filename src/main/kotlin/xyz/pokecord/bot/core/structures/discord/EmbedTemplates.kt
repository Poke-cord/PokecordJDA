package xyz.pokecord.bot.core.structures.discord

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.I18n
import xyz.pokecord.bot.core.structures.discord.base.BaseCommandContext

open class EmbedTemplates {
  enum class Color(val code: Int) {
    GREEN(0x2ecc71),
    RED(0xf04747),
    YELLOW(0xf0e365)
  }

  open suspend fun translate(key: String, vararg data: Pair<String, String>): String {
    return I18n.translate(null, key, *data)
  }

  open suspend fun translate(key: String, default: String, vararg data: Pair<String, String>): String {
    return I18n.translate(null, key, default, *data)
  }

  open suspend fun translate(key: String, data: Map<String, String>, default: String?): String {
    return I18n.translate(null, key, data, default)
  }

  fun empty() = EmbedBuilder().setColor(Color.YELLOW.code)

  suspend fun error(description: String, title: String? = null): EmbedBuilder {
    val embedTitle = title ?: translate("misc.embeds.error.title")
    return EmbedBuilder().setColor(Color.RED.code).setTitle(embedTitle).setDescription(description)
      .setFooter(translate("misc.embeds.error.footer"))
  }

  fun confirmation(description: String, title: String? = null) =
    EmbedBuilder().setColor(0xfaa61a).setDescription(description).setTitle(
      title
    )

  fun normal(description: String, title: String? = null): EmbedBuilder {
    return empty().setDescription(description).setTitle(title)
  }

  suspend fun progressPrivate(user: User) = error(
    translate("misc.embeds.progressPrivate.description", "user" to user.asMention),
    translate("misc.embeds.progressPrivate.title")
  )

  open suspend fun start() = normal(
    translate("misc.checks.hasStarted.embed.description", "prefix" to "p!"),
    translate("misc.checks.hasStarted.embed.title")
  )
}

class ContextEmbedTemplates(private val context: ICommandContext) : EmbedTemplates() {
  override suspend fun translate(key: String, vararg data: Pair<String, String>): String {
    return context.translate(key, *data)
  }

  override suspend fun translate(key: String, default: String, vararg data: Pair<String, String>): String {
    return context.translate(key, default, *data)
  }

  override suspend fun translate(key: String, data: Map<String, String>, default: String?): String {
    return context.translate(key, data, default)
  }

  override suspend fun start() = normal(
    translate("misc.checks.hasStarted.embed.description", "prefix" to context.getPrefix()),
    translate("misc.checks.hasStarted.embed.title")
  )
}