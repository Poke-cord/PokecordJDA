package xyz.pokecord.bot.core.managers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml

//class I18n {
//  private val defaultLanguage = Language.EN_US
//
//  private val locales: MutableMap<String, Properties> = mutableMapOf()
//  private val logger: Logger = LoggerFactory.getLogger(I18n::class.java)
//
//  init {
//    val languageNames = Language.values().map { langEnum -> langEnum.identifier }
//    languageNames.forEach {
//      val stream = I18n::class.java.getResourceAsStream("/i18n/$it.properties")
//      if (stream != null) {
//        val properties = Properties()
//        properties.load(stream)
//        locales[it] = Properties()
//        properties.forEach { property ->
//          locales[it]?.setProperty(property.key.toString().toLowerCase(), property.value.toString())
//        }
//        logger.info("Loaded language '$it' with ${properties.size} entries.")
//      } else {
//        logger.error("Failed to load language '$it' because a corresponding resource file was not found.")
//      }
//    }
//  }
//
//  fun translate(language: Language, key: String, vararg data: Pair<String, String>) =
//    translate(language, key, mapOf(*data))
//
//  fun translate(language: Language, key: String, data: Map<String, String>): String {
//    val lowerCaseKey = key.toLowerCase()
//    var string = locales[language.identifier]?.getProperty(lowerCaseKey)
//    if (string == null && language != defaultLanguage) string =
//      locales[defaultLanguage.identifier]?.getProperty(lowerCaseKey)
//    if (string == null) return lowerCaseKey
//    for (dataKey in data.keys) {
//      string = string?.replace("{{$dataKey}}", data[dataKey] ?: "")
//    }
//    return string ?: lowerCaseKey
//  }
//
//  enum class Language(val identifier: String, val pokeApiLanguageId: Int? = null) {
//    JA_JP("ja-JP", 1),
//    EN_US("en-US", 9)
//  }
//}

object I18n {
  private val defaultLanguage = Language.EN_US

  private val locales: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
  private val logger: Logger = LoggerFactory.getLogger(I18n::class.java)

  private val yaml = Yaml()

  init {
    val languageNames = Language.values().map { langEnum -> langEnum.identifier }
    languageNames.forEach {
      val stream = I18n::class.java.getResourceAsStream("/i18n/$it.yaml")
      if (stream != null) {
        val obj: Map<String, Any> = yaml.load(stream)
        locales[it] = mutableMapOf()
        addRecursively(obj, locales[it]!!)
        logger.info("Loaded language '$it' with ${locales[it]!!.size} entries.")
      } else {
        logger.error("Failed to load language '$it' because a corresponding resource file was not found.")
      }
    }
  }

  private fun addRecursively(
    obj: Map<*, *>,
    target: MutableMap<String, String>,
    key: String = ""
  ) {
    obj.forEach { entry ->
      if (entry.value is String) {
        target["$key.${entry.key}".toLowerCase()] = entry.value as String
      } else if (entry.value is Map<*, *>) {
        addRecursively(entry.value as Map<*, *>, target, if (key == "") entry.key.toString() else "$key.${entry.key}")
      }
    }
  }

  fun translate(language: Language?, key: String, vararg data: Pair<String, String>) =
    translate(language, key, mapOf(*data))

  fun translate(language: Language?, key: String, default: String, vararg data: Pair<String, String>) =
    translate(language, key, mapOf(*data), default)

  fun translate(language: Language?, key: String, data: Map<String, String>, default: String? = null): String {
    val lowerCaseKey = key.toLowerCase()
    val lang = language ?: Language.default
    var string = locales[lang.identifier]?.get(lowerCaseKey)
    if (string == null && lang != defaultLanguage) string =
      locales[defaultLanguage.identifier]?.get(lowerCaseKey)
    if (string == null) return lowerCaseKey
    for (dataKey in data.keys) {
      string = string?.replace("{{$dataKey}}", data[dataKey] ?: "")
    }
    return string ?: default ?: lowerCaseKey
  }

  enum class Language(val identifier: String, val pokeApiLanguageId: Int? = null) {
    //    JA_JP("ja-JP", 1),
    EN_US("en-US", 9);

    companion object {
      val default
        get() = EN_US
    }
  }
}
