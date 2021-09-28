package xyz.pokecord.bot.core.managers.database.repositories

import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.*

class TradeRepository(
  database: Database,
  private val collection: CoroutineCollection<Trade>,
) : Repository(database) {
  suspend fun createTrade(traderId: String, tradedId: String) {
    val trader = TraderData(traderId, tradedId)
    val traded = TraderData(tradedId, traderId)
    collection.insertOne(
      Trade(trader, traded)
    )
  }

  suspend fun getTrade(userId: String): Trade? {
    return collection.findOne(
      or(
        Trade::initiator / TraderData::userId eq userId,
        Trade::receiver / TraderData::userId eq userId
      )
    )
  }

  suspend fun getTraderData(userId: String): TraderData? {
    val trade = collection.findOne(
      or(
        Trade::initiator / TraderData::userId eq userId,
        Trade::receiver / TraderData::userId eq userId
      )
    )

    return if(trade != null) {
      if(trade.initiator.userId == userId) {
        trade.initiator
      } else {
        trade.receiver
      }
    } else null
  }

  suspend fun deleteTrade(userId: String) {
    collection.deleteOne(
      or(
        Trade::initiator / TraderData::userId eq userId,
        Trade::receiver / TraderData::userId eq userId
      )
    )
  }

  suspend fun confirm(trade: Trade, traderId: String) {
    if(trade.initiator.userId == traderId) {
      collection.updateOne(
        Trade::_id eq trade._id,
        set(Trade::initiator / TraderData::confirmed setTo true)
      )
    } else {
      collection.updateOne(
        Trade::_id eq trade._id,
        set(Trade::receiver / TraderData::confirmed setTo true)
      )
    }
  }

  suspend fun incCredits(trade: Trade, traderId: String, amount: Int) {
    if(trade.initiator.userId == traderId) {
      collection.updateOne(
        Trade::_id eq trade._id,
        inc(Trade::initiator / TraderData::credits, amount)
      )
    } else {
      collection.updateOne(
        Trade::_id eq trade._id,
        inc(Trade::receiver / TraderData::credits, amount)
      )
    }
  }

  suspend fun addPokemon(trade: Trade, traderId: String, pokemonId: Id<OwnedPokemon>) {
    if(trade.initiator.userId == traderId) {
      collection.updateOne(
        Trade::_id eq trade._id,
        push(Trade::initiator / TraderData::pokemon, pokemonId)
      )
    } else {
      collection.updateOne(
        Trade::_id eq trade._id,
        push(Trade::receiver / TraderData::pokemon, pokemonId)
      )
    }
  }

  suspend fun removePokemon(trade: Trade, traderId: String, pokemonId: Id<OwnedPokemon>) {
    if(trade.initiator.userId == traderId) {
      collection.updateOne(
        Trade::_id eq trade._id,
        pull(Trade::initiator / TraderData::pokemon, pokemonId)
      )
    } else {
      collection.updateOne(
        Trade::_id eq trade._id,
        pull(Trade::receiver / TraderData::pokemon, pokemonId)
      )
    }
  }
}