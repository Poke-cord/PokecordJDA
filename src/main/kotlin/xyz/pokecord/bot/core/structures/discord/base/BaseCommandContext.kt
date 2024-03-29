package xyz.pokecord.bot.core.structures.discord.base

import io.sentry.Breadcrumb
import io.sentry.Sentry
import net.dv8tion.jda.api.entities.MessageEmbed
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.I18n
import xyz.pokecord.bot.core.managers.database.models.*
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.ContextEmbedTemplates
import xyz.pokecord.bot.core.structures.discord.Translator
import xyz.pokecord.bot.utils.Config
import xyz.pokecord.bot.utils.Confirmation
import xyz.pokecord.bot.utils.PokemonResolvable
import xyz.pokecord.bot.utils.extensions.awaitSuspending
import java.lang.reflect.InvocationTargetException
import net.dv8tion.jda.api.entities.User as JDAUser

abstract class BaseCommandContext(override val bot: Bot) : ICommandContext {
  protected var language: I18n.Language? = null
  protected var prefix: String? = null
  private var guildData: Guild? = null
  protected var userData: User? = null

  override val sentryBreadcrumbs = mutableListOf<Pair<Breadcrumb, Any?>>()
  override val embedTemplates by lazy { ContextEmbedTemplates(this) }
  override val translator by lazy { Translator(this) }

  override fun shouldProcess(): Boolean {
    if (author.isBot) return false
    return Config.devs.contains(author.id) || (if (Config.officialServerOnlyMode) isFromGuild && Config.officialServers.contains(
      guild!!.id
    ) else true)
  }

  override suspend fun translate(key: String, vararg data: Pair<String, String>): String {
    return I18n.translate(getLanguage(), key, *data)
  }

  override suspend fun translate(key: String, default: String, vararg data: Pair<String, String>): String {
    return I18n.translate(getLanguage(), key, default, *data)
  }

  override suspend fun translate(key: String, data: Map<String, String>, default: String?): String {
    return I18n.translate(getLanguage(), key, data, default)
  }

  override suspend fun getUserData(forceFetch: Boolean): User {
    if (userData == null || forceFetch) {
      userData = bot.database.userRepository.getUser(author)
    }
    return userData!!
  }

  override suspend fun getTradeState(): Trade? {
    return bot.database.tradeRepository.getTrade(author.id)
  }

  override suspend fun getReleaseState(): Release? {
    return bot.database.releaseRepository.getRelease(author.id)
  }

  override suspend fun getTraderState(): TraderData? {
    return bot.database.tradeRepository.getTraderData(author.id)
  }

  override suspend fun getBattleState(): Battle? {
    return bot.database.battleRepository.getUserCurrentBattle(author)
  }

  override suspend fun getGuildData(forceFetch: Boolean): Guild? {
    if (!isFromGuild) return null
    if (guildData == null || forceFetch) {
      guildData = bot.database.guildRepository.getGuild(guild!!)
    }
    return guildData
  }

  override suspend fun getLanguage(): I18n.Language {
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

  override suspend fun getPrefix(): String {
    return jda.selfUser.asMention
  }

  override suspend fun hasStarted(sendMessage: Boolean): Boolean {
    val userData = getUserData()
    if (userData.selected == null) {
      if (sendMessage) {
        reply(embedTemplates.start().build()).queue()
      }
      return false
    }
    return true
  }

  override suspend fun resolvePokemon(
    jdaUser: JDAUser,
    userData: User,
    pokemonResolvable: PokemonResolvable?
  ): OwnedPokemon? {
    if (pokemonResolvable?.data == null) {
      val selectedPokemonId = userData.selected ?: return null
      return bot.database.pokemonRepository.getPokemonById(selectedPokemonId)
    }
    if (pokemonResolvable is PokemonResolvable.Int) {
      return bot.database.pokemonRepository.getPokemonByIndex(jdaUser.id, (pokemonResolvable.data as Int) - 1)
    }
    if (pokemonResolvable is PokemonResolvable.Latest) {
      return bot.database.pokemonRepository.getLatestPokemon(jdaUser.id)
    }
    if (pokemonResolvable is PokemonResolvable.Ivs) {
      return bot.database.pokemonRepository.getPokemonByTotalIv(jdaUser.id, (pokemonResolvable.data as Int))
    }
    return null
  }

//  fun addBreadcrumb(breadcrumb: Breadcrumb, hint: Any? = null) {
//    sentryBreadcrumbs += Pair(breadcrumb, hint)
//  }

  override suspend fun handleException(
    exception: Throwable,
    module: Module?,
    command: Command?,
    event: Event?,
    extras: Map<String, String>
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
      module?.let { scope.setExtra("module", it.name) }
      command?.let { command ->
        scope.setExtra("command", command.name)
        command.parentCommand?.let { parentCommand ->
          scope.setExtra("parentCommand", parentCommand.name)
        }
      }
      event?.let { event ->
        scope.setExtra("event", event.name)
      }
      extras.forEach {
        scope.setExtra(it.key, it.value)
      }
      if (this.isFromGuild) {
        scope.setExtra("guildId", guild!!.id)
        scope.setExtra("guildName", guild!!.name)
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
          "Error occurred while doing something unknown"
        }
      }
      logger.error(
        message,
        if (exception is InvocationTargetException) exception.targetException else exception
      )
      errorEmbed?.let { reply(it) }
    }
  }

  override suspend fun askForTOSAgreement(): Boolean {
    val confirmation = Confirmation(this)
    return confirmation.result(
      embedTemplates.normal(
        translate(
          "misc.embeds.rules.description",
          mapOf(
            "botUsername" to jda.selfUser.name,
            "user" to author.asMention,
            "tosUrl" to "https://sites.google.com/view/pokecord4908/english/terms",
            "privacyUrl" to "https://sites.google.com/view/pokecord4908/english/privacy"
          )
        ),
        translate("misc.embeds.rules.title")
      )
        .setFooter(translate("misc.embeds.rules.footer"))
    )
  }

  override suspend fun isStaff(): Boolean {
//    return Config.devs.contains(author.id) || (guild?.id == Config.mainServer && bot.cache.staffMemberIds.containsAsync(
//      author.id
//    ).awaitSuspending())
    return Config.devs.contains(author.id) || bot.cache.staffMemberIds.containsAsync(author.id).awaitSuspending()
  }

  companion object {
    protected val logger: Logger = LoggerFactory.getLogger(ICommandContext::class.java)
  }
}
