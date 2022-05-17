package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.utils.PokemonStats
import kotlin.math.pow
import kotlin.math.roundToInt

@Serializable
data class Battle(
  val initiator: Trainer,
  val partner: Trainer,
  val channelId: String,
  val wager: Int?,
  val startedAtMillis: Long = System.currentTimeMillis(),
  val endedAtMillis: Long? = null,
  @Contextual val _id: Id<Battle> = newId()
) {
  val winner: Trainer?
    get() = if (initiator.pokemonStats.hp <= 0) partner
    else if (partner.pokemonStats.hp <= 0) initiator
    else null

  val shouldUseMove: Boolean
    get() = initiator.pendingMove != null && partner.pendingMove != null

  fun hasTrainer(id: String): Boolean {
    return initiator.id == id || partner.id == id
  }

  fun getTrainers(selfId: String): Pair<Trainer, Trainer> {
    val self: Trainer
    val partner: Trainer
    if (selfId == initiator.id) {
      self = initiator
      partner = this.partner
    } else {
      self = this.partner
      partner = initiator
    }
    return self to partner
  }

  companion object {
    fun gainedCredits(wager: Int): Int {
      return (((wager * 2) * 1.25) * 0.75).roundToInt()
    }

    fun gainedXp(
      selfId: String,
      selfPokemon: Pokemon,
      selfPokemonTrainerId: String,
      selfPokemonLevel: Int,
      partnerPokemonLevel: Int
    ): Int {
      // https://bulbapedia.bulbagarden.net/wiki/Experience#Gain_formula
      val a = 1.0
      val b = (selfPokemon.baseExp ?: 0).toDouble()
      val l = partnerPokemonLevel.toDouble()
      val lp = selfPokemonLevel.toDouble()
      val s = 1.0
      val t = if (selfPokemonTrainerId === selfId) 1.0 else 1.5
      val e = 1.0 // TODO: add lucky egg check if/when it's added
      val p = 1.0 // TODO: add exp point power check if/when it's added
      return ((((a * b * l) / (5 * s)) * ((2 * l + 10).pow(2.5) / (l + lp + 10).pow(2.5)) + 1) * t * e * p).roundToInt()
    }
  }

  @Serializable
  data class MoveResult(
    val defenderDamage: Int = 0,
    val selfDamage: Int = 0,
    val typeEffectiveness: Double = 0.0,
    val isMissed: Boolean = false,
    val isCritical: Boolean = false,
  ) {
    val nothingHappened: Boolean
      get() = defenderDamage == 0 && selfDamage == 0
  }

  @Serializable
  data class Request(
    val initiatorId: String,
    val partnerId: String,
    val initiatedChannelId: String,
    val wager: Int?,
    val initiatedAtMillis: Long = System.currentTimeMillis()
  ) {
    val uniqueId
      get() = "$initiatedChannelId-$initiatedAtMillis" // assuming this will be unique enough for our case
  }

  @Serializable
  data class Trainer(
    val id: String,
    val pokemonId: Int,
    var pokemonStats: PokemonStats,
    var pendingMove: Int? = null,
    val recentlyUsedMoves: MutableList<Int> = mutableListOf(),
    val recentMoveResults: MutableList<MoveResult> = mutableListOf(),
  )
}
