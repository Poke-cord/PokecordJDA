package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.utils.Json
import kotlin.system.exitProcess

@Serializable
data class Experience(
  val growthRateId: Int,
  val level: Int,
  val experience: Int
) {
  companion object {
    val items: List<Experience>

    init {
      val stream = Experience::class.java.getResourceAsStream("/data/experience.json")
      if (stream == null) {
        println("Experience data not found. Exiting...")
        exitProcess(0)
      }
      val json = stream.readAllBytes().decodeToString()
      items = Json.decodeFromString(json)
    }
  }
}
