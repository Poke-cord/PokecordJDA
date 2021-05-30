package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.utils.Json
import kotlin.system.exitProcess

@Serializable
data class PokemonEvolution(
  val id: Int,
  val evolvedSpeciesId: Int,
  val evolutionTriggerId: Int,
  val triggerItemId: Int,
  val minimumLevel: Int,
  val genderId: Int,
  val locationId: Int,
  val heldItemId: Int,
  val timeOfDay: String,
  val knownMoveId: Int,
  val knownMoveTypeId: Int,
  val minimumHappiness: Int,
  val minimumBeauty: Int,
  val minimumAffection: Int,
  val relativePhysicalStats: Int,
  val partySpeciesId: Int,
  val partyTypeId: Int,
  val tradeSpeciesId: Int,
  val needsOverworldRain: Boolean,
  val turnUpsideDown: Boolean
) {
  companion object {
    val items: List<PokemonEvolution>

    init {
      val stream = PokemonEvolution::class.java.getResourceAsStream("/data/pokemon_evolution.json")
      if (stream == null) {
        println("Pokemon evolution data not found. Exiting...")
        exitProcess(0)
      }
      val json = stream.readAllBytes().decodeToString()
      items = Json.decodeFromString(json)
    }
  }
}
