package xyz.pokecord.bot.core.managers.database.repositories

import com.mongodb.client.model.Indexes
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.Order

class OrderRepository(
  database: Database,
  private val orderCollection: CoroutineCollection<Order>
) : Repository(database) {
  override suspend fun createIndexes() {
    orderCollection.createIndex(Indexes.ascending("userId"))
  }

  suspend fun getUnpaidOrder(userId: String): Order? {
    return orderCollection.findOne(Order::userId eq userId)
  }

  suspend fun createOrder(orderData: Order) {
    orderCollection.insertOne(orderData)
  }

  suspend fun deleteOrder(orderData: Order) {
    orderCollection.deleteOne(Order::_id eq orderData._id)
  }
}
