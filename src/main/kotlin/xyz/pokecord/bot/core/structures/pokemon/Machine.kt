package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.utils.Json
import kotlin.system.exitProcess

@Serializable
data class Machine(
  val tmId: Int,
  val versionGroupId: Int,
  val itemId: Int,
  val moveId: Int
) {
  companion object {
    private val items: List<Machine>

    init {
      val stream = Machine::class.java.getResourceAsStream("/data/machines.json")
      if (stream == null) {
        println("Machine data not found. Exiting...")
        exitProcess(0)
      }
      val json = stream.readAllBytes().decodeToString()
      items = Json.decodeFromString(json)
    }

    fun getById(id: Int) = items.find { it.tmId == id }
    fun getByItemId(id: Int) = items.find { it.itemId == id }
    fun getByMoveId(id: Int) = items.find { it.moveId == id }
  }
}
