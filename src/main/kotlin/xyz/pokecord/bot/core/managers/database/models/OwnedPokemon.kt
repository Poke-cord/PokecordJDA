package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import xyz.pokecord.bot.core.structures.pokemon.Experience
import xyz.pokecord.bot.core.structures.pokemon.Nature
import xyz.pokecord.bot.core.structures.pokemon.Species
import xyz.pokecord.bot.core.structures.pokemon.Stat
import xyz.pokecord.bot.utils.PokemonStats
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.Random
import xyz.pokecord.bot.core.structures.pokemon.Pokemon as PokemonData

@Serializable
data class OwnedPokemon(
  var id: Int,
  var index: Int,
  val ownerId: String,
  val shiny: Boolean,
  val trainerId: String? = null,
  var level: Int = defaultLevel(),
  val nature: String = defaultNature(),
  var ivs: PokemonStats = defaultIV(),
  var xp: Int = 0,
  var gender: Int = 2, // hack for default gender, see the init block below
  val heldItemId: Int = 0,
  var moves: MutableList<Int> = mutableListOf(0, 0, 0, 0),
  var favorite: Boolean = false,
  val rewardClaimed: Boolean = false,
  val timestamp: Long = System.currentTimeMillis(),
  val sticky: Boolean = false,
  var nickname: String? = null,
  @Contextual val _id: Id<OwnedPokemon> = newId(),
  @Contextual val originalId: Id<OwnedPokemon>? = null
) {
  init {
    if (gender < -1 || gender > 1) {
      gender = defaultGender(id)
    }
  }

  val totalIv = ivs.total

  val displayName
    get() = "$name${if (shiny) " ⭐" else ""}"

  val imageUrl
    get() = PokemonData.getImageUrl(id, shiny)

  val ivPercentage
    get() = "${"%.2f".format((totalIv.toDouble() / 186) * 100)}%"

  private val name
    get() = nickname ?: data.species.name?.name ?: "N/A"

  val data
    get() = PokemonData.getById(id)!!

  val stats: PokemonStats by lazy {
    val nature = Nature.getByName(nature)!!
    val hp = getStatValue(Stat.hp, nature, ivs.hp)
    val attack = getStatValue(Stat.attack, nature, ivs.attack)
    val defense = getStatValue(Stat.defense, nature, ivs.defense)
    val specialAttack = getStatValue(Stat.specialAttack, nature, ivs.specialAttack)
    val specialDefense = getStatValue(Stat.specialDefense, nature, ivs.specialDefense)
    val speed = getStatValue(Stat.speed, nature, ivs.speed)
    PokemonStats(attack, defense, hp, specialAttack, specialDefense, speed)
  }

  fun requiredXpToLevelUp(): Int {
    if (level >= 100) return 0
    val experienceEntries =
      Experience.items.filter { it.growthRateId == data.species.growthRateId && (it.level == level || it.level == level + 1) }
    val currentLevelExperienceEntry = experienceEntries.find { it.level == level }
    val nextLevelExperienceEntry = experienceEntries.find { it.level == level + 1 }
    if (currentLevelExperienceEntry == null || nextLevelExperienceEntry == null) return 0
    return nextLevelExperienceEntry.experience - currentLevelExperienceEntry.experience
  }

  private fun getStatValue(stat: Stat, nature: Nature, iv: Int, ev: Int = stat.getBaseEffortValue(id)!!): Int {
    val base = stat.getBaseValue(id)!!

    val statVal = if (stat.identifier == "hp") {
      if (id == 292) 1
      else floor(
        floor((floor((2 * base + iv + ev / 4).toDouble()) * level) / 100) +
            level +
            10
      ).roundToInt()
    } else {
      (floor(floor(floor((2 * base + iv + ev / 4).toDouble()) * level) / 100) + 5).roundToInt()
    }
    var multiplier = 1.0

    if (nature.increasedStatId == stat.id) multiplier += 0.1
    if (nature.decreasedStatId == stat.id) multiplier -= 0.1
    return floor(statVal * multiplier).roundToInt()
  }

  companion object {
    private fun defaultGender(speciesId: Int): Int {
      val genderRate = Species.getById(speciesId)?.genderRate
      return genderRate?.let {
        if (it == -1) -1 else if (Random.nextFloat() < (it * 12.5) / 100) 0 else 1
      } ?: 0
    }

    private fun defaultIV(): PokemonStats {
      return PokemonStats(
        Random.nextInt(0, 32),
        Random.nextInt(0, 32),
        Random.nextInt(0, 32),
        Random.nextInt(0, 32),
        Random.nextInt(0, 32),
        Random.nextInt(0, 32)
      )
    }

    private fun defaultLevel() = Random.nextInt(1, 41)

    private fun defaultNature() = Nature.random() ?: "Brave"
  }
}
