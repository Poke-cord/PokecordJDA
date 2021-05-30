package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.utils.Json
import kotlin.system.exitProcess

@Serializable
data class MoveMeta(
  val id: Int,
  val metaCategoryId: Int,
  val metaAilmentId: Int,
  val minHits: Int,
  val maxHits: Int,
  val minTurns: Int,
  val maxTurns: Int,
  val drain: Int,
  val healing: Int,
  val criticalRate: Int,
  val ailmentChance: Int,
  val flinchChance: Int,
  val statChance: Int
) {
  companion object {
    private val items: List<MoveMeta>

    init {
      val stream = MoveMeta::class.java.getResourceAsStream("/data/move_meta.json")
      if (stream == null) {
        println("Move meta data not found. Exiting...")
        exitProcess(0)
      }
      val json = stream.readAllBytes().decodeToString()
      items = Json.decodeFromString(json)
    }

    fun getById(id: Int) = items.find { it.id == id }
  }
}
