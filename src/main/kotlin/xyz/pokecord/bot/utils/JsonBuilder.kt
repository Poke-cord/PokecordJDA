package xyz.pokecord.bot.utils

sealed class JsonElement(private val value: Any? = null) {
  override fun toString() = value.toString()

  class JsonBoolean(value: Boolean) : JsonElement(value)
  class JsonNumber(value: Number) : JsonElement(value)
  class JsonNull : JsonElement(null)

  class JsonString(private val value: String) : JsonElement(value) {
    override fun toString() = "\"$value\""
  }

  class JsonArray(
    private val indent: Int,
    private val initialIndent: Int = indent
  ) : JsonElement() {
    private val items = mutableListOf<JsonElement>()

    fun array(init: JsonArray.() -> Unit) {
      val jsonArray = JsonArray(indent + initialIndent, initialIndent = initialIndent)
      jsonArray.init()
      items.add(jsonArray)
    }

    fun boolean(value: Boolean?) {
      items.add(if (value == null) JsonNull() else JsonBoolean(value))
    }

    fun number(value: Number?) {
      items.add(if (value == null) JsonNull() else JsonNumber(value))
    }

    fun json(init: JsonObject.() -> Unit) {
      val indentedJson = JsonObject(indent = indent + initialIndent, initialIndent = initialIndent)
      indentedJson.init()
      items.add(indentedJson)
    }

    fun string(value: String?) {
      items.add(if (value == null) JsonNull() else JsonString(value.replace("\"", "\\\"")))
    }

    override fun toString(): String {
      val lineBreak = if (indent == 0) "" else "\n"
      val content = items.joinToString(",${lineBreak}") { "${" ".repeat(indent)}${it}" }
      return "[${lineBreak}$content${lineBreak}${" ".repeat(indent - initialIndent)}]"
    }
  }

  class JsonObject(
    private val indent: Int = 2,
    private val initialIndent: Int = indent
  ) : JsonElement() {
    private var map = LinkedHashMap<String, JsonElement?>()

    fun array(key: String, init: (JsonArray.() -> Unit)?) {
      if (init == null) map[key] = null
      else {
        val jsonArray = JsonArray(indent + initialIndent, initialIndent = initialIndent)
        jsonArray.init()
        map[key] = jsonArray
      }
    }

    fun boolean(key: String, value: Boolean?) {
      map[key] = if (value == null) JsonNull() else JsonBoolean(value)
    }

    fun number(key: String, value: Number?) {
      map[key] = if (value == null) JsonNull() else JsonNumber(value)
    }

    fun json(key: String, init: (JsonObject.() -> Unit)?) {
      if (init == null) map[key] = null
      else {
        val indentedJson = JsonObject(indent = indent + initialIndent, initialIndent = initialIndent)
        indentedJson.init()
        map[key] = indentedJson
      }
    }

    fun string(key: String, value: String?) {
      map[key] = if (value == null) JsonNull() else JsonString(value.replace("\"", "\\\""))
    }

    override fun toString(): String {
      val lineBreak = if (indent == 0) "" else "\n"
      val content = map.map {
        "${" ".repeat(indent)}\"${it.key}\": ${it.value ?: "\"null\""}"
      }.joinToString(",${lineBreak}")
      return "{${lineBreak}$content${lineBreak}${" ".repeat(indent - initialIndent)}}"
    }
  }
}

fun jsonObject(indent: Int = 2, init: JsonElement.JsonObject.() -> Unit): JsonElement.JsonObject {
  val json = JsonElement.JsonObject(indent)
  json.init()
  return json
}

fun jsonArray(indent: Int = 2, init: JsonElement.JsonArray.() -> Unit): JsonElement.JsonArray {
  val json = JsonElement.JsonArray(indent)
  json.init()
  return json
}

private fun main() {
  val json = jsonObject {
    string("this", "that")
    string("ok", null)
    boolean("hmm", true)
    boolean("ok", false)
    boolean("nice", null)
    number("num", 4)
    json("inner") {
      string("nested", "object")
      string("that has", "strings")
      number("and integers", 96)
      boolean("booleans too btw", true)
    }
    array("and arrays?") {
      string("totally works")
      boolean(true)
      number(99.99)
      json {
        string("nested", "object")
        string("inside", "array")
      }
      array {
        string("woah")
        string("nested array in array?? is that cool?")
        boolean(true)
      }
    }
  }
  println(json)

  val array = jsonArray(4) {
    string("top-level")
    string("arrays are")
    string("supported too")
    number(100)
    json {
      string("nested", "object")
      string("inside", "array")
    }
    array {
      string("so are")
      string("bottom-level arrays")
      boolean(null)
    }
  }

  println(array)
}
