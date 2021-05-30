package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.utils.Json
import kotlin.system.exitProcess

@Serializable
data class MoveData(
  val id: Int,
  val identifier: String,
  val name: String,
  val generationId: Int,
  val typeId: Int,
  val power: Int,
  val pp: Int,
  val accuracy: Int,
  val priority: Int,
  val targetId: Int,
  val damageClassId: Int,
  val effectId: Int,
  val effectChance: Int,
  val contestTypeId: Int,
  val contestEffectId: Int,
  val superContestEffectId: Int,
  val romanGenerationId: String
) {
  val meta by lazy {
    MoveMeta.getById(id)!!
  }

  val type by lazy {
    Type.getById(typeId)!!
  }

  companion object {
    private val items: List<MoveData>

    init {
      val stream = MoveData::class.java.getResourceAsStream("/data/moves.json")
      if (stream == null) {
        println("Move data not found. Exiting...")
        exitProcess(0)
      }
      val json = stream.readAllBytes().decodeToString()
      items = Json.decodeFromString(json)
    }

    fun getById(id: Int) = items.find { it.id == id }

    fun getByName(name: String): MoveData? {
      return items.find { it.name.equals(name, true) || it.identifier.equals(name, true) }
    }
  }
}
