package xyz.pokecord.bot.modules.developer.commands

import net.dv8tion.jda.api.entities.ChannelType
import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext
import xyz.pokecord.bot.modules.developer.DeveloperCommand
import javax.script.Compilable
import javax.script.ScriptContext
import javax.script.ScriptEngineManager

class EvalCommand : DeveloperCommand() {
  override val name = "Eval"

  private val codeRegex = "```([a-z]+)[\\s\\n](.+)[\\s\\n]```".toRegex(RegexOption.MULTILINE)
  private val scriptEngineManager = ScriptEngineManager()

  @Executor
  fun execute(
    context: MessageReceivedContext,
    @Argument(consumeRest = true, name = "code") input: String?
  ) {
    val groupValues = input?.let { codeRegex.matchEntire(it)?.groupValues }
    val extension = groupValues?.get(1)
    val code = groupValues?.get(2)

    if (extension == null) {
      context.reply(
        """
        ```
        No extension was provided
        ```
      """.trimIndent()
      ).queue()
      return
    }

    if (code == null) {
      context.reply(
        """
        ```
        No code was provided to evaluate
        ```
      """.trimIndent()
      ).queue()
      return
    }

    val scriptEngine = scriptEngineManager.getEngineByExtension(extension)
    if (scriptEngine == null) {
      context.reply(
        """
        No script engine found for extension "$extension"
      """.trimIndent()
      ).queue()
      return
    }
    if (scriptEngine.factory.engineName == "Graal.js") {
      val bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE)
      bindings["polyglot.js.allowHostAccess"] = true
    }
    scriptEngine.put("context", context)
    scriptEngine.put("jda", context.jda)
    scriptEngine.put("message", context.message)
    scriptEngine.put("guild", if (context.isFromType(ChannelType.TEXT)) context.guild else null)
    scriptEngine.put("channel", context.channel)
    scriptEngine.put("bot", module.bot)
    scriptEngine.put("module", module)

    try {
      val result = (if (scriptEngine is Compilable) scriptEngine.compile(code).eval() else scriptEngine.eval(code))
      context.reply(
        """Evaluated Successfully:
```
$result ```"""
      ).queue()
    } catch (e: Exception) {
      context.reply(
        """An exception was thrown:
```
$e ```"""
      ).queue()
    }
  }
}
