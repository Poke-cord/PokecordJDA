package xyz.pokecord.bot.api

import io.sentry.Breadcrumb
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.requests.RestAction
import xyz.pokecord.bot.core.managers.I18n
import xyz.pokecord.bot.core.managers.database.models.Guild
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.managers.database.models.User
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.EmbedTemplates
import xyz.pokecord.bot.core.structures.discord.Translator
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.discord.base.Event
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.utils.PokemonResolvable
import java.time.OffsetDateTime
import net.dv8tion.jda.api.entities.Guild as JDAGuild
import net.dv8tion.jda.api.entities.User as JDAUser

interface ICommandContext {
  val author: JDAUser
  val bot: Bot
  val channel: MessageChannel
  val embedTemplates: EmbedTemplates
  val event: GenericEvent
  val guild: JDAGuild?
  val isFromGuild: Boolean
  val jda: JDA
  val sentryBreadcrumbs: MutableList<Pair<Breadcrumb, Any?>>
  val timeCreated: OffsetDateTime
  val translator: Translator

  suspend fun getGuildData(): Guild?
  suspend fun getLanguage(): I18n.Language
  suspend fun getPrefix(): String
  suspend fun getUserData(): User
  suspend fun handleException(
    exception: Throwable,
    module: Module,
    command: Command? = null,
    event: Event? = null,
    extras: Map<String, String> = mapOf()
  )

  fun addActionRows(vararg actionRows: ActionRow): ICommandContext
  fun clearActionRows(): ICommandContext

  suspend fun hasStarted(sendMessage: Boolean = false): Boolean
  fun reply(content: String, mentionRepliedUser: Boolean = false): RestAction<*>
  fun reply(embed: MessageEmbed, mentionRepliedUser: Boolean = false): RestAction<*>
  suspend fun resolvePokemon(jdaUser: JDAUser, userData: User, pokemonResolvable: PokemonResolvable?): OwnedPokemon?
  fun shouldProcess(): Boolean
  suspend fun translate(key: String, data: Map<String, String>, default: String? = null): String
  suspend fun translate(key: String, vararg data: Pair<String, String>): String
  suspend fun translate(key: String, default: String, vararg data: Pair<String, String>): String
}