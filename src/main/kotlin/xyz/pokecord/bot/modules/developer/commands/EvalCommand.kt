package xyz.pokecord.bot.modules.developer.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.MessageCommandContext
import xyz.pokecord.bot.modules.developer.DeveloperCommand
import javax.script.Compilable
import javax.script.ScriptContext
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class EvalCommand : DeveloperCommand() {
  override val name = "Eval"

  private val codeRegex =
    "```([a-z]+)[\\s\\n](.+)[\\s\\n]```".toRegex(setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL))
  private val scriptEngineManager = ScriptEngineManager()

  private val scriptEngines = mutableMapOf<String, ScriptEngine>()

  @Executor
  suspend fun execute(context: ICommandContext) {
    if (context !is MessageCommandContext) return

    val input = context.event.message.contentRaw.drop(context.getPrefix().length + name.length).trim()

    val groupValues = input.let { codeRegex.matchEntire(it)?.groupValues }
    var extension = groupValues?.get(1) ?: "kts"
    var code = groupValues?.get(2)

    if (extension == "kt") extension = "kts"

//    if (extension == null) {
//      context.reply(
//        """
//        ```
//        No extension was provided
//        ```
//      """.trimIndent()
//      ).queue()
//      return
//    }

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

    var scriptEngine = scriptEngines[extension]
    if (scriptEngine == null) {
      scriptEngine = scriptEngineManager.getEngineByExtension(extension)
      if (scriptEngine != null) scriptEngines[extension] = scriptEngine
    }

    if (scriptEngine == null) {
      context.reply(
        """
        No script engine found for extension "$extension"
      """.trimIndent()
      ).queue()
      return
    }

    if (scriptEngine.factory.engineName == "kotlin") {
      code = """
        import kotlinx.coroutines.runBlocking
        
        $code
      """.trimIndent()
    }


    scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).apply {
      if (scriptEngine.factory.engineName == "Graal.js") {
        put("polyglot.js.allowHostAccess", true)
      }

      put("context", context)
      context.guild?.let { put("guild", it) }
      put("module", module)
    }

    try {
      val now = System.currentTimeMillis()
      val result = (if (scriptEngine is Compilable) scriptEngine.compile(code).eval() else scriptEngine.eval(code))
      println(System.currentTimeMillis() - now)
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
