package xyz.pokecord.bot.core.structures.discord

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.Cache
import xyz.pokecord.bot.core.managers.I18n
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.structures.HTTPServer
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.utils.Config
import xyz.pokecord.bot.utils.api.Discord
import xyz.pokecord.bot.utils.api.PayPal
import xyz.pokecord.bot.utils.api.TopGG
import java.util.concurrent.Executors
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberFunctions

class Bot constructor(private val token: String, private val topggToken: String? = null) {
  lateinit var shardManager: ShardManager
  lateinit var commandHandler: CommandHandler

  private lateinit var version: String

  val hostname = System.getenv("HOSTNAME") ?: System.getenv("COMPUTERNAME") ?: "Unknown"

  val devEnv = System.getenv("DEV").equals("true", true)

  val payPal by lazy { PayPal(database) }
  val discordRestClient by lazy { Discord(token) }
  val topggClient by lazy { topggToken?.let { TopGG(it) } }
  val backgroundCoroutineScope =
    CoroutineScope(Executors.newFixedThreadPool(2).asCoroutineDispatcher() + SupervisorJob())

  val cache: Cache = Cache()
  val database: Database = Database(cache)
  val logger: Logger = LoggerFactory.getLogger(Bot::class.java)
  val modules = linkedMapOf<String, Module>()

  private var started = false
  var maintenance = devEnv

  val httpServer by lazy { HTTPServer(this) }

  fun toggleMaintenance() {
    maintenance = !maintenance
    updatePresence()
  }

  fun start(shardManagerBuilder: DefaultShardManagerBuilder, build: Boolean = false) {
    httpServer.start()

    this.version = if (devEnv) "DEV" else Config.version
    val intents = mutableListOf(
      GatewayIntent.DIRECT_MESSAGES,
      GatewayIntent.DIRECT_MESSAGE_REACTIONS,
      GatewayIntent.GUILD_MESSAGES,
      GatewayIntent.GUILD_MESSAGE_REACTIONS,
    )
    val moduleArray = modules.values.filter { it.enabled }.map { it }.toTypedArray()
    for (module in moduleArray) {
      for (intent in module.intents) {
        if (!intents.contains(intent)) {
          intents.add(intent)
        }
      }
      module.load()
    }
    commandHandler = CommandHandler(this)
    shardManagerBuilder
      .addEventListeners(commandHandler, *moduleArray)
      .setEnabledIntents(intents)
    if (build) {
      shardManager = shardManagerBuilder.build()
    }
    started = true
  }

  private suspend fun getListHelpEmbedLine(
    context: ICommandContext,
    prefix: String,
    command: Command
  ): String {
    val name =
      (if (command.parentCommand != null) "${command.parentCommand!!.name} ${command.name}" else command.name).lowercase()
    val commandDescription = getCommandDescription(context, command)
    return "**${prefix}${name}**${if (command.usage.isEmpty()) "" else " `${command.usage}` "}${if (commandDescription.isEmpty()) "" else " - $commandDescription "}"
  }

  private suspend fun getCommandDescription(context: ICommandContext, command: Command): String {
    return context.translate(command.descriptionI18nKey, "")
  }

  suspend fun getHelpEmbed(context: ICommandContext, module: Module, prefix: String = "p!"): EmbedBuilder? {
    if (!module.enabled) return null
    val commandEntries = mutableListOf<String>()
    for (command in module.commands) {
      if (!command.enabled) continue
      if (!command.canRun(context)) continue
      if (command.parentCommand != null) continue
      if (command is ParentCommand) {
        command.childCommands.forEach { childCommand ->
          if (!childCommand.excludeFromHelp) commandEntries.add(getListHelpEmbedLine(context, prefix, childCommand))
        }
      }
      if (!command.excludeFromHelp) commandEntries.add(getListHelpEmbedLine(context, prefix, command))
    }
    if (commandEntries.isNotEmpty()) {
      return context.embedTemplates.normal(
        commandEntries.joinToString("\n"),
        "${module.name} ${context.translate("misc.texts.commands")}"
      )
    }
    return null
  }

  suspend fun getHelpEmbed(context: ICommandContext, command: Command): EmbedBuilder? {
    if (!command.enabled || command.excludeFromHelp || !command.canRun(context)) return null
    val descriptionLines = mutableListOf<String>()
    val commandDescription = getCommandDescription(context, command)
    if (commandDescription.isNotEmpty()) {
      descriptionLines.add("${commandDescription}\n")
    }
    if (command.usage.isNotEmpty()) {
      descriptionLines.add("**${context.translate("misc.texts.usage")}**: `${command.usage}`\n")
    }
    // TODO: command note support
    if (command.aliases.isNotEmpty()) {
      val aliasesString = command.aliases.joinToString(", ") { "`${it}`" }
      descriptionLines.add("**${context.translate("misc.texts.command_aliases")}**: $aliasesString")
      val executor = command::class.memberFunctions.find { it.hasAnnotation<Command.Executor>() }
      if (executor != null) {
        val argumentAliasesLines = executor.parameters
          .mapNotNull { param ->
            val annotation = param.findAnnotation<Command.Argument>() ?: return@mapNotNull null
            if (annotation.aliases.isEmpty()) null
            else "`${if (annotation.name == "") param.name else annotation.name}` - ${
              annotation.aliases.joinToString(", ") { "`${it}`" }
            }"
          }
        if (argumentAliasesLines.isNotEmpty()) {
          descriptionLines += "**${context.translate("misc.texts.argument_aliases")}**\n" + argumentAliasesLines.joinToString(
            "\n"
          )
        }
      }
    }
    return context.embedTemplates.normal(
      descriptionLines.joinToString("\n"),
      "${command.parentCommand?.name ?: ""} ${command.name} ${context.translate("misc.texts.command")} - ${command.module.name}".trim()
    )
  }

  suspend fun getHelpEmbeds(context: ICommandContext, prefix: String = "p!"): List<EmbedBuilder> {
    return modules.mapNotNull { getHelpEmbed(context, it.value, prefix) }
  }

  suspend fun getHelpEmbeds(context: ICommandContext, commands: List<Command>): List<EmbedBuilder> {
    return commands.mapNotNull { getHelpEmbed(context, it) }
  }

  suspend fun getChildrenCommandListEmbed(
    context: ICommandContext,
    parentCommand: ParentCommand,
    commands: List<Command>,
    prefix: String = "p!"
  ): EmbedBuilder? {
    val commandEntries = mutableListOf<String>()
    for (command in commands) {
      if (!command.enabled) continue
      if (!command.canRun(context)) continue
      if (command is ParentCommand) {
        command.childCommands.forEach { childClass ->
          val childCommand = command.module.commands.find { it::class == childClass }
          if (childCommand != null) {
            if (!childCommand.excludeFromHelp) commandEntries.add(getListHelpEmbedLine(context, prefix, childCommand))
          }
        }
      }
      if (!command.excludeFromHelp) commandEntries.add(getListHelpEmbedLine(context, prefix, command))
    }
    if (commandEntries.isNotEmpty()) {
      return context.embedTemplates.normal(
        commandEntries.joinToString("\n"),
        "${parentCommand.name} ${context.translate("misc.texts.commands")}"
      )
    }
    return null
  }

  fun updatePresence() {
    val activityTextData = mapOf(
      "prefix" to (commandHandler.prefix),
      "version" to (if (devEnv) "vBETA" else "v$version")
    )

    val activityTranslationKey =
      if (maintenance) "misc.presence.maintenance.activity" else "misc.presence.regular.activity"
    val statusTranslationKey = if (maintenance) "misc.presence.maintenance.status" else "misc.presence.regular.status"
    val typeTranslationKey = if (maintenance) "misc.presence.maintenance.type" else "misc.presence.regular.type"
    val urlTranslationKey = if (maintenance) "misc.presence.maintenance.url" else "misc.presence.regular.url"

    val activity =
      I18n.translate(null, activityTranslationKey, activityTextData)
    val status = I18n.translate(null, statusTranslationKey)
    val type = I18n.translate(null, typeTranslationKey)
    val url = I18n.translate(null, urlTranslationKey)

    try {
      shardManager.setStatus(OnlineStatus.valueOf(status))
    } catch (e: IllegalArgumentException) {
      shardManager.setStatus(if (maintenance) OnlineStatus.DO_NOT_DISTURB else OnlineStatus.ONLINE)
    }
    try {
      shardManager.setActivity(Activity.of(Activity.ActivityType.valueOf(type), activity, url))
    } catch (e: IllegalArgumentException) {
      shardManager.setActivity(
        Activity.playing("${commandHandler.prefix}help | ${if (devEnv) "vBETA" else "v$version"}")
      )
    }
  }

  // TODO: use it somewhere maybe
  @Suppress("UNUSED")
  fun shutdown() {
    backgroundCoroutineScope.cancel()
    cache.shutdown()
    database.close()
    if (this::shardManager.isInitialized) {
      shardManager.shutdown()
    }
  }
}
