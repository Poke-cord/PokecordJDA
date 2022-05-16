package xyz.pokecord.bot.core.managers.database.repositories

import com.mongodb.client.model.Indexes
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.dv8tion.jda.api.entities.User
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.redisson.api.RMapCacheAsync
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.Battle
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.structures.pokemon.MoveData
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.PokemonStats
import xyz.pokecord.bot.utils.extensions.awaitSuspending
import java.util.concurrent.TimeUnit

class BattleRepository(
  database: Database,
  private val collection: CoroutineCollection<Battle>,
  private val battleRequestCacheMap: RMapCacheAsync<String, String>
) : Repository(database) {
  override suspend fun createIndexes() {
    collection.createIndex(Indexes.ascending("initiatorId"))
    collection.createIndex(Indexes.ascending("partnerId"))
  }

  suspend fun getUserCurrentBattle(user: User) = getUserCurrentBattle(user.id)
  private suspend fun getUserCurrentBattle(userId: String): Battle? {
    return collection.findOne(
      or(
        Battle::initiator / Battle.Trainer::id eq userId,
        Battle::partner / Battle.Trainer::id eq userId
      ),
      Battle::endedAtMillis eq null
    )
  }

  suspend fun initiateBattleRequest(
    initiatorId: String, partnerId: String, initiatedChannelId: String, wager: Int?
  ): Battle.Request {
    val battleRequest = Battle.Request(initiatorId, partnerId, initiatedChannelId, wager)
    battleRequestCacheMap.putAsync(battleRequest.uniqueId, Json.encodeToString(battleRequest), 30, TimeUnit.SECONDS)
      .awaitSuspending()
    return battleRequest
  }

  suspend fun findBattleRequest(
    initiatedChannelId: String,
    initiatedAtMillis: Long
  ): Battle.Request? {
    return battleRequestCacheMap.getAsync("$initiatedChannelId-$initiatedAtMillis").awaitSuspending()
      ?.let { Json.decodeFromString(it) }
  }

  suspend fun acceptBattleRequest(
    battleRequest: Battle.Request,
    initiatorPokemonId: Int,
    initiatorPokemonStats: PokemonStats,
    partnerPokemonId: Int,
    partnerPokemonStats: PokemonStats,
  ): Battle {
    val battle = Battle(
      Battle.Trainer(battleRequest.initiatorId, initiatorPokemonId, initiatorPokemonStats),
      Battle.Trainer(battleRequest.partnerId, partnerPokemonId, partnerPokemonStats),
      battleRequest.initiatedChannelId
    )
    collection.insertOne(battle)
    return battle
  }

  suspend fun rejectBattleRequest(battleRequest: Battle.Request) {
    battleRequestCacheMap.removeAsync(battleRequest.uniqueId).awaitSuspending()
  }

  suspend fun getBattle(id: Id<Battle>): Battle? {
    return collection.findOneById(id)
  }

  suspend fun chooseMove(battle: Battle, trainerId: String, moveId: Int) {
    val trainer = if (battle.initiator.id == trainerId) battle.initiator else battle.partner
    trainer.pendingMove = moveId
    val trainerProperty = if (battle.initiator.id == trainerId) Battle::initiator else Battle::partner
    collection.updateOneById(
      battle._id,
      set(
        trainerProperty / Battle.Trainer::pendingMove setTo trainer.pendingMove,
      )
    )
  }

  suspend fun useMove(
    battle: Battle,
    attackerId: String,
    attackerPokemon: OwnedPokemon,
    defenderPokemon: OwnedPokemon,
    move: MoveData
  ): Battle.MoveResult {
    val (attacker, defender) = battle.getTrainers(attackerId)
    val moveResult = move.use(
      attackerPokemon.data,
      attackerPokemon.level,
      attacker.pokemonStats,
      defenderPokemon.data,
      defender.pokemonStats,
      defender.recentlyUsedMoves,
      defender.recentMoveResults
    )
    attacker.recentMoveResults.add(moveResult)
    attacker.recentlyUsedMoves.add(move.id)
    attacker.pokemonStats =
      attacker.pokemonStats.copy(hp = (attacker.pokemonStats.hp - moveResult.selfDamage).coerceAtLeast(0))
    defender.pokemonStats =
      defender.pokemonStats.copy(hp = (defender.pokemonStats.hp - moveResult.defenderDamage).coerceAtLeast(0))
    battle.initiator.pendingMove = null
    battle.partner.pendingMove = null
    collection.updateOneById(
      battle._id,
      set(
        Battle::initiator setTo battle.initiator,
        Battle::partner setTo battle.partner,
      )
    )
    return moveResult
  }

  suspend fun endBattle(battle: Battle) {
    // update credits for the winner

    collection.updateOneById(
      battle._id,
      set(
        Battle::endedAtMillis setTo System.currentTimeMillis()
      )
    )
  }
}
