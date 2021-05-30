package xyz.pokecord.bot.core.structures.discord

import io.sentry.Breadcrumb
import io.sentry.Sentry
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.LoggerFactory
import xyz.pokecord.bot.core.managers.I18n
import xyz.pokecord.bot.core.managers.database.models.Guild
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.managers.database.models.User
import xyz.pokecord.bot.utils.Config
import xyz.pokecord.bot.utils.PokemonResolvable
import java.lang.reflect.InvocationTargetException
import net.dv8tion.jda.api.entities.User as JDAUser

class MessageReceivedContext(val bot: Bot, api: JDA, responseNumber: Long, message: Message) :
  MessageReceivedEvent(api, responseNumber, message) {
  private var language: I18n.Language? = null
  private var prefix: String? = null
  private var guildData: Guild? = null
  var userData: User? = null

  private val sentryBreadcrumbs = mutableListOf<Pair<Breadcrumb, Any?>>()
  val embedTemplates = EmbedTemplates(this)
  val translator = Translator(this)

  fun reply(content: Message, mentionRepliedUser: Boolean = false) =
    message.reply(content).mentionRepliedUser(mentionRepliedUser)

  fun reply(content: CharSequence, mentionRepliedUser: Boolean = false) =
    message.reply(content).mentionRepliedUser(mentionRepliedUser)

  fun reply(embed: MessageEmbed, mentionRepliedUser: Boolean = false) =
    message.reply(embed).mentionRepliedUser(mentionRepliedUser)

  fun shouldProcess(): Boolean {
    return Config.devs.contains(author.id) || (if (Config.officialServerOnlyMode) isFromGuild && Config.officialServers.contains(
      guild.id
    ) else true)
  }

  suspend fun getUserData(): User {
    if (userData == null) {
      userData = bot.database.userRepository.getUser(author)
      userData!!.tag = author.asTag
    }
    return userData!!
  }

  suspend fun getGuildData(): Guild? {
    if (!isFromGuild) return null
    if (guildData == null) {
      guildData = bot.database.guildRepository.getGuild(guild)
    }
    return guildData
  }

  suspend fun getLanguage(): I18n.Language {
    if (language == null) {
      val user = getUserData()
      if (user.language != null) {
        language = user.language
      } else if (isFromGuild) {
        val guild = getGuildData()!!
        if (guild.language != null) {
          language = guild.language
        }
      }
    }
    language = language ?: I18n.Language.EN_US
    return language!!
  }

  suspend fun getPrefix(): String {
    if (prefix == null) {
      prefix = if (isFromGuild) {
        val guild = getGuildData()!!
        guild.prefix ?: bot.commandHandler.prefix
      } else bot.commandHandler.prefix
    }
    return prefix!!
  }

  suspend fun hasStarted(sendMessage: Boolean = false): Boolean {
    val userData = getUserData()
    if (userData.selected == null) {
      if (sendMessage) {
        message.reply(embedTemplates.start().build()).queue()
      }
      return false
    }
    return true
  }

  suspend fun translate(key: String, vararg data: Pair<String, String>): String {
    return bot.translate(getLanguage(), key, *data)
  }

  suspend fun translate(key: String, data: Map<String, String>): String {
    return bot.translate(getLanguage(), key, data)
  }

  suspend fun resolvePokemon(jdaUser: JDAUser, userData: User, pokemonResolvable: PokemonResolvable?): OwnedPokemon? {
    if (pokemonResolvable == null || pokemonResolvable.data == null) {
      val selectedPokemonId = userData.selected ?: return null
      return bot.database.pokemonRepository.getPokemonById(selectedPokemonId)
    }
    if (pokemonResolvable is PokemonResolvable.Int) {
      return bot.database.pokemonRepository.getPokemonByIndex(jdaUser.id, (pokemonResolvable.data as Int) - 1)
    }
    if (pokemonResolvable is PokemonResolvable.Latest) {
      return bot.database.pokemonRepository.getLatestPokemon(jdaUser.id)
    }
    return null
  }

  fun addBreadcrumb(breadcrumb: Breadcrumb, hint: Any? = null) {
    sentryBreadcrumbs += Pair(breadcrumb, hint)
  }

  suspend fun handleException(
    exception: Throwable,
    module: Module,
    command: Command? = null,
    event: Event? = null
  ) {
    var errorEmbed: MessageEmbed? = null
    if (command != null) {
      bot.cache.setRunningCommand(author.id, false)
      errorEmbed = embedTemplates.error(
        translate(
          "misc.errors.embeds.commandExecutionFailed.description",
          "command" to command.name
        ),
        translate("misc.errors.embeds.commandExecutionFailed.title")
      )
        .build()
    }
    Sentry.withScope { scope ->
      scope.user = io.sentry.protocol.User().apply {
        id = author.id
        username = author.asTag
      }
      scope.setExtra("messageContent", message.contentRaw)
      scope.setExtra("module", module.name)
      command?.let { command ->
        scope.setExtra("command", command.name)
        command.parentCommand?.let { parentCommand ->
          scope.setExtra("parentCommand", parentCommand.name)
        }
      }
      event?.let { event ->
        scope.setExtra("event", event.name)
      }
      scope.setExtra("messageId", message.id)
      scope.setExtra("channelId", channel.id)
      if (isFromGuild) {
        scope.setExtra("guildId", guild.id)
        scope.setExtra("guildName", guild.name)
        scope.setExtra("channelName", channel.name)
      }
      scope.setExtra("shardInfo", jda.shardInfo.shardString)
      sentryBreadcrumbs.forEach {
        scope.addBreadcrumb(it.first, it.second)
      }
      val message = when {
        command != null -> {
          "Error occurred while executing the ${command.name} command"
        }
        event != null -> {
          "Error occurred while handling the ${event.name} event"
        }
        else -> {
          "Error occurred while doing something unknown within the ${module.name} module"
        }
      }
      logger.error(
        message,
        if (exception is InvocationTargetException) exception.targetException else exception
      )
      errorEmbed?.let { reply(it) }
    }
  }

  companion object {
    private val logger = LoggerFactory.getLogger(MessageReceivedContext::class.java)
  }
}
