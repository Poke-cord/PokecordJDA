package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.core.managers.database.models.Battle
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.PokemonStats
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.random.Random
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

  fun use(
    attackerPokemon: Pokemon,
    attackerPokemonLevel: Int,
    attackerPokemonStats: PokemonStats,
    defenderPokemon: Pokemon,
    defenderPokemonStats: PokemonStats,
    defenderRecentlyUsedMoves: List<Int>,
    defenderRecentMoveResults: List<Battle.MoveResult>,
  ): Battle.MoveResult {
    var effectivePower = power

    when (identifier) {
      "sonic-boom" -> return Battle.MoveResult(20)
      "counter" -> {
        if (defenderRecentMoveResults.isEmpty()) return Battle.MoveResult(isMissed = true)
        val lastMoveResult = defenderRecentMoveResults.last()
        val lastMoveId = defenderRecentlyUsedMoves.last()
        val lastMove = getById(lastMoveId)!!
        if (lastMoveResult.isMissed || lastMove.damageClassId != 2) return Battle.MoveResult(isMissed = true)
        if (defenderPokemon.types.any { it.identifier == "ghost" }) return Battle.MoveResult(isMissed = true)

        if (lastMoveResult.defenderDamage > 0) return Battle.MoveResult(lastMoveResult.defenderDamage * 2)
        else effectivePower = 1
      }
      "seismic-toss", "night-shade" -> return Battle.MoveResult(attackerPokemonLevel)
      "dragon-rage" -> return Battle.MoveResult(40)
      "psywave" -> {
        val r = round(Random.nextDouble(100.0))
        val damage = ((attackerPokemonLevel * (r + 50)) / 100).roundToInt().coerceAtLeast(1)
        return Battle.MoveResult(damage)
      }
      "super-fang" -> return Battle.MoveResult((defenderPokemonStats.hp / 2.0).roundToInt().coerceAtLeast(1))
      "mirror-coat" -> {
        if (defenderRecentMoveResults.isEmpty()) return Battle.MoveResult(isMissed = true)
        val lastMoveResult = defenderRecentMoveResults.last()
        val lastMoveId = defenderRecentlyUsedMoves.last()
        val lastMove = getById(lastMoveId)!!

        return if (defenderPokemon.types.any { it.identifier == "dark" } || lastMoveResult.defenderDamage <= 0 || lastMove.damageClassId != 3) {
          Battle.MoveResult()
        } else {
          Battle.MoveResult(lastMoveResult.defenderDamage * 2)
        }
      }
      "endeavor" -> return if (attackerPokemonStats.hp >= defenderPokemonStats.hp) {
        Battle.MoveResult()
      } else {
        Battle.MoveResult(
          defenderPokemonStats.hp - attackerPokemonStats.hp
        )
      }
      "metal-burst" -> {
        if (defenderRecentMoveResults.isEmpty()) return Battle.MoveResult(isMissed = true)
        val lastMoveResult = defenderRecentMoveResults.last()
        return if (lastMoveResult.isMissed) {
          Battle.MoveResult()
        } else {
          Battle.MoveResult((lastMoveResult.defenderDamage * 1.5).roundToInt())
        }
      }
      "heavy-slam" -> {
        val defendersRelativeWeight = (defenderPokemon.weight.toDouble() / attackerPokemon.weight) * 100

        effectivePower = when {
          defendersRelativeWeight > 50 -> 40
          defendersRelativeWeight >= 33.35 -> 60
          defendersRelativeWeight >= 25.01 -> 80
          defendersRelativeWeight >= 20.01 -> 100
          else -> 120
        }
      }
      "electro-ball" -> {
        val speedPercentage = (100.0 / attackerPokemonStats.speed) * defenderPokemonStats.speed
        effectivePower = when (speedPercentage) {
          in 0.01..25.0 -> 150
          in 25.01..33.33 -> 120
          in 33.34..50.0 -> 80
          in 50.01..100.0 -> 60
          else -> 40
        }
      }
      "final-gambit" -> return Battle.MoveResult(attackerPokemonStats.hp, attackerPokemonStats.hp)
      "guardian-of-alola" -> return Battle.MoveResult((attackerPokemonStats.hp / 100) * 75)
      "natures-madness" -> return Battle.MoveResult((defenderPokemonStats.hp / 2).coerceAtLeast(1))
    }

    val stab = if (attackerPokemon.types.any { it.id == typeId }) 1.5 else 1.0
    val typeEffectiveness = getTypeEffectiveness(defenderPokemon.types)
    val isCritical = if (meta.criticalRate > 0) {
      Random.nextDouble() < 1.0 / 8.0
    } else {
      Random.nextDouble() < 1.0 / 24.0
    }
    val criticalMultiplier = if (isCritical) 1.5 else 1.0
    val randomMultiplier = Random.nextDouble(0.85, 1.0)
    val modifier = criticalMultiplier * randomMultiplier * stab * typeEffectiveness

    val result = floor(
      of32(
        floor(
          of32(
            of32(floor(((2 * attackerPokemonLevel) / 5 + 2).toDouble()) * effectivePower) * (when (damageClassId) {
              2 -> attackerPokemonStats.attack.toDouble()
              3 -> attackerPokemonStats.specialAttack.toDouble()
              else -> 0.0
            })
          ) / (when (damageClassId) {
            2 -> defenderPokemonStats.defense
            3 -> defenderPokemonStats.specialDefense
            else -> 0
          })
        ) / 50 + 2
      )
    ).coerceAtLeast(0.0)

    return Battle.MoveResult(
      floor(result * modifier).toInt(),
      isCritical = isCritical,
      typeEffectiveness = typeEffectiveness
    )
  }

  // 32 bit integer OVERFLOW PROTECTION
  private fun of32(n: Double): Double {
    return if (n > 0xffffffff) n % 0x100000000 else n
  }

  private fun getTypeEffectiveness(defenderPokemonTypes: List<Type>): Double {
    val effectiveness = mutableListOf<Double>()
    val typeEfficacy = TypeEfficacy.getById(typeId)!!
    for (type in defenderPokemonTypes) {
      val efficacy = typeEfficacy.efficacies.find { it.targetTypeId == type.id }
      if (efficacy != null) {
        effectiveness.add(efficacy.damageFactor / 100.0)
      }
    }
    if (effectiveness.isEmpty()) {
      return if (type.identifier == "shadow") {
        if (defenderPokemonTypes.any { it.identifier == "shadow" }) 0.5 else 2.0
      } else {
        1.0
      }
    }

    return effectiveness.reduce { a, b -> a * b }.coerceAtLeast(0.0)
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
