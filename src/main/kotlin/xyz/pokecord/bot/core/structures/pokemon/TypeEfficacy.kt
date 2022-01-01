package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.utils.Json
import kotlin.system.exitProcess

@Serializable
data class TypeEfficacy(
  val id: Int,
  val efficacies: List<Efficacy>
) {
  companion object {
    private val items: List<TypeEfficacy>

    init {
      val stream = TypeEfficacy::class.java.getResourceAsStream("/data/type_efficacy.json")
      if (stream == null) {
        println("Type data not found. Exiting...")
        exitProcess(0)
      }
      val json = stream.readAllBytes().decodeToString()
      items = Json.decodeFromString(json)
    }

    fun getById(id: Int) = items.find { it.id == id }
  }

  @Serializable
  data class Efficacy(
    val targetTypeId: Int,
    val damageFactor: Double
  )
}
