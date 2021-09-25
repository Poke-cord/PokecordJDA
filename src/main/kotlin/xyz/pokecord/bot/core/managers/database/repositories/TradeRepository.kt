package xyz.pokecord.bot.core.managers.database.repositories

import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.inc
import org.litote.kmongo.or
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

  suspend fun deleteTrade(userId: String) {
    collection.deleteOne(
      or(
        Trade::initiator / TraderData::userId eq userId,
        Trade::receiver / TraderData::userId eq userId
      )
    )
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
}