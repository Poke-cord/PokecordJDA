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

  suspend fun deleteTrade(trade: Trade) {
    collection.deleteOne(Trade::_id eq trade._id)
  }

  suspend fun confirm(trade: Trade, traderId: String) {
    collection.updateOne(
      Trade::_id eq trade._id,
      set((if (trade.initiator.userId == traderId) Trade::initiator else Trade::receiver) / TraderData::confirmed setTo true)
    )
  }

  suspend fun incCredits(trade: Trade, traderId: String, amount: Int) {
    collection.updateOne(
      Trade::_id eq trade._id,
      inc((if (trade.initiator.userId == traderId) Trade::initiator else Trade::receiver) / TraderData::credits, amount)
    )
  }

  suspend fun addPokemon(trade: Trade, traderId: String, pokemonId: Id<OwnedPokemon>) {
    collection.updateOne(
      Trade::_id eq trade._id,
      push((if(trade.initiator.userId == traderId) Trade::initiator else Trade::receiver) / TraderData::pokemon, pokemonId)
    )
  }

  suspend fun removePokemon(trade: Trade, traderId: String, pokemonId: Id<OwnedPokemon>) {
    collection.updateOne(
      Trade::_id eq trade._id,
      pull((if(trade.initiator.userId == traderId) Trade::initiator else Trade::receiver) / TraderData::pokemon, pokemonId)
    )
  }
}