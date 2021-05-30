package xyz.pokecord.bot.core.structures.discord

import dev.minn.jda.ktx.injectKTX
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.pokecord.bot.core.managers.Cache
import xyz.pokecord.bot.core.managers.I18n
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.utils.extensions.removeAccents
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberFunctions

class Bot constructor(private val token: String) {
  lateinit var jda: JDA
  lateinit var commandHandler: CommandHandler

  private lateinit var version: String

  private val devEnv = System.getenv("DEV").equals("true", true)
  private val i18N: I18n = I18n()

  val cache: Cache = Cache()
  val database: Database = Database(cache)
  val logger: Logger = LoggerFactory.getLogger(Bot::class.java)
  val modules = linkedMapOf<String, Module>()

  private var started = false
  var maintenance = devEnv

  fun start(shardCount: Int? = null, shardId: Int? = null) {
    this.version = this.javaClass.`package`.implementationVersion ?: "DEV"
    val intents = mutableListOf(
      GatewayIntent.DIRECT_MESSAGES,
      GatewayIntent.DIRECT_MESSAGE_REACTIONS,
      GatewayIntent.GUILD_MESSAGES,
      GatewayIntent.GUILD_MESSAGE_REACTIONS
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
    var jdaBuilder = JDABuilder.createDefault(token)
      .injectKTX()
      .setStatus(OnlineStatus.DO_NOT_DISTURB)
      .setActivity(Activity.playing("Loading..."))
      .addEventListeners(commandHandler, *moduleArray)
      .setEnabledIntents(intents)
    if (shardCount != null && shardId != null) {
      jdaBuilder = jdaBuilder.useSharding(shardId, shardCount)
    }
    jda = jdaBuilder.build()
    started = true
  }

  private suspend fun getModuleHelpEmbedLine(
    context: MessageReceivedContext,
    prefix: String,
    command: Command
  ): String {
    val name =
      (if (command.parentCommand != null) "${command.parentCommand!!.name} ${command.name}" else command.name).toLowerCase()
    val commandDescription = getCommandDescription(context, command)
    return "**${prefix}${name}**${if (command.usage.isEmpty()) "" else " `${command.usage}` "}${if (commandDescription.isEmpty()) "" else " - $commandDescription "}"
  }

  private suspend fun getCommandDescription(context: MessageReceivedContext, command: Command): String {
    val key =
      if (command.parentCommand != null) "${command.module.name}.${command.parentCommand!!.name}.${command.name}"
      else "${command.module.name}.${command.name}"
    return context.translate("misc.command_descriptions.${key.removeAccents()}")
  }

  suspend fun getHelpEmbed(context: MessageReceivedContext, module: Module, prefix: String = "p!"): EmbedBuilder {
    val commandEntries: ArrayList<String> = arrayListOf()
    for (command in module.commands) {
      if (!command.canRun(context)) continue
      if (command::class.hasAnnotation<Command.ChildCommand>()) continue
      if (command is ParentCommand) {
        command.childCommands.forEach { childClass ->
          val childCommand = module.commands.find { it::class == childClass }
          if (childCommand != null) {
            if (!childCommand.excludeFromHelp) commandEntries.add(getModuleHelpEmbedLine(context, prefix, childCommand))
          }
        }
      }
      if (!command.excludeFromHelp) commandEntries.add(getModuleHelpEmbedLine(context, prefix, command))
    }
    return context.embedTemplates.normal(
      commandEntries.joinToString("\n"),
      "${module.name} ${context.translate("misc.texts.commands")}"
    )
  }

  suspend fun getHelpEmbed(context: MessageReceivedContext, command: Command): EmbedBuilder? {
    if (!command.enabled || command.excludeFromHelp || !command.canRun(context)) return null
    val descriptionLines = arrayListOf<String>()
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

  suspend fun getHelpEmbeds(context: MessageReceivedContext, prefix: String = "p!"): List<EmbedBuilder> {
    return modules.map { getHelpEmbed(context, it.value, prefix) }
  }

  suspend fun getHelpEmbeds(context: MessageReceivedContext, commands: List<Command>): List<EmbedBuilder> {
    return commands.mapNotNull { getHelpEmbed(context, it) }
  }

  fun translate(
    language: I18n.Language? = null,
    key: String,
    vararg data: Pair<String, String>
  ): String {
    return i18N.translate(language ?: I18n.Language.EN_US, key, *data)
  }

  fun translate(
    language: I18n.Language? = null,
    key: String,
    data: Map<String, String>
  ): String {
    return i18N.translate(language ?: I18n.Language.EN_US, key, data)
  }

  fun updatePresence() {
    val activityTextData = mapOf(
      "prefix" to (commandHandler.prefix),
      "version" to (if (devEnv) "Development Edition" else "v$version")
    )

    val activityTranslationKey =
      if (maintenance) "misc.presence.maintenance.activity" else "misc.presence.regular.activity"
    val statusTranslationKey = if (maintenance) "misc.presence.maintenance.status" else "misc.presence.regular.status"
    val typeTranslationKey = if (maintenance) "misc.presence.maintenance.type" else "misc.presence.regular.type"
    val urlTranslationKey = if (maintenance) "misc.presence.maintenance.url" else "misc.presence.regular.url"

    val activity =
      translate(null, activityTranslationKey, activityTextData)
    val status = translate(null, statusTranslationKey)
    val type = translate(null, typeTranslationKey)
    val url = translate(null, urlTranslationKey)

    try {
      jda.presence.setStatus(OnlineStatus.valueOf(status))
    } catch (e: IllegalArgumentException) {
      jda.presence.setStatus(if (maintenance) OnlineStatus.DO_NOT_DISTURB else OnlineStatus.ONLINE)
    }
    try {
      jda.presence.activity = Activity.of(Activity.ActivityType.valueOf(type), activity, url)
    } catch (e: IllegalArgumentException) {
      jda.presence.activity =
        Activity.playing("${commandHandler.prefix}help | pokecord.xyz | ${if (devEnv) "Development Edition" else "v$version"}")
    }
  }
}
