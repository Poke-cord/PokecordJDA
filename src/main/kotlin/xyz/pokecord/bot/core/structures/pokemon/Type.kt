package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.utils.Json
import kotlin.system.exitProcess

@Serializable
data class Type(
  val id: Int,
  val identifier: String,
  val generationId: Int,
  val damageClassId: Int,
  val romanGenerationId: String,
) {
  @Transient
  val name = getName()

  fun getName(languageId: Int = 9) =
    names.find { it.id == id && it.languageId == languageId }

  companion object {
    private val items: List<Type>
    private val names: List<Name>

    init {
      val namesStream = Type::class.java.getResourceAsStream("/data/type_names.json")
      if (namesStream == null) {
        println("Type names not found. Exiting...")
        exitProcess(0)
      }
      val namesJson = namesStream.readAllBytes().decodeToString()
      names = Json.decodeFromString(namesJson)
      val stream = Type::class.java.getResourceAsStream("/data/types.json")
      if (stream == null) {
        println("Type data not found. Exiting...")
        exitProcess(0)
      }
      val json = stream.readAllBytes().decodeToString()
      items = Json.decodeFromString(json)
    }

    fun getById(id: Int) = items.find { it.id == id }

    fun getByName(name: String): Type? {
      val id = names.find { it.name.equals(name, true) }?.id ?: return null
      return getById(id)
    }
  }
}
