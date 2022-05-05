package xyz.pokecord.bot.core.managers.database.repositories

import com.mongodb.client.model.Indexes
import org.litote.kmongo.combine
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.replaceOne
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
    return orderCollection.findOne(combine(Order::userId eq userId, Order::paid eq false))
  }

  suspend fun createOrder(orderData: Order) {
    orderCollection.insertOne(orderData)
  }

  suspend fun replaceOrder(orderData: Order) {
    orderCollection.replaceOne(orderData)
  }

  suspend fun getOrder(orderId: String): Order? {
    return orderCollection.findOne(Order::orderId eq orderId)
  }

  suspend fun getOrdersByUser(userId: String): List<Order> {
    return orderCollection.find(Order::userId eq userId).toList()
  }

  suspend fun deleteOrder(orderData: Order) {
    orderCollection.deleteOne(Order::_id eq orderData._id)
  }
}
