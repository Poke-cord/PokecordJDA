package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import xyz.pokecord.bot.core.managers.database.Database
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
  var moves: MutableList<Int> = mutableListOf(),
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
    if (moves.isEmpty()) {
      repeat(4) { moves.add(0) }
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

  enum class TransferStates(var errMessage: String) {
    STICKY("You cannot transfer this pokemon because its sticky."),
    FAVORITE("You cannot transfer this pokemon because its favorited."),
    TRADE_SESSION("You cannot transfer this pokemon because its in a trade session."),
    NO_POKEMON("You cannot transfer this pokemon because its the only one you have."),
    SELECTED("You cannot transfer this pokemon because you have it selected."),
    SUCCESS("The pokemon is transferable.")
  }

  suspend fun transferable(database: Database): TransferStates {
    if(this.sticky) return TransferStates.STICKY
    if(this.favorite) return TransferStates.FAVORITE

    val tradeData = database.tradeRepository.getTraderData(this.ownerId)
    if(tradeData != null) {
      if(tradeData.pokemon.contains(this._id)) {
        return TransferStates.TRADE_SESSION
      }
    }

    val userData = database.userRepository.getUser(ownerId)
    if(userData.pokemonCount <= 1) return TransferStates.NO_POKEMON
    if(userData.selected == this._id) return TransferStates.SELECTED

    return TransferStates.SUCCESS
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

  private fun getStatValue(stat: Stat, nature: Nature, iv: Int, ev: Int = stat.getBaseEffortValue(id)!!) =
    getStatValue(id, level, stat, nature, iv, ev)

  companion object {
    fun getStatValue(
      id: Int,
      level: Int,
      stat: Stat,
      nature: Nature,
      iv: Int,
      ev: Int = stat.getBaseEffortValue(id)!!
    ): Int {
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

    private fun defaultGender(speciesId: Int): Int {
      val genderRate = Species.getById(speciesId)?.genderRate
      return genderRate?.let {
        if (it == -1) -1 else if (Random.nextFloat() < (it * 12.5) / 100) 0 else 1
      } ?: 0
    }

    fun defaultIV(): PokemonStats {
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
