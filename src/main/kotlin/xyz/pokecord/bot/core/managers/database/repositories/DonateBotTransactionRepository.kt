package xyz.pokecord.bot.core.managers.database.repositories

import com.mongodb.client.model.Indexes
import org.litote.kmongo.coroutine.CoroutineCollection
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.DonateBotTransaction

class DonateBotTransactionRepository(
  database: Database,
  private val donateBotTransactionCollection: CoroutineCollection<DonateBotTransaction>
) : Repository(database) {
  override suspend fun createIndexes() {
    donateBotTransactionCollection.createIndex(Indexes.ascending("txn_id"))
    donateBotTransactionCollection.createIndex(Indexes.ascending("buyer_id"))
  }

  suspend fun createTransaction(transactionData: DonateBotTransaction) {
    donateBotTransactionCollection.insertOne(transactionData)
  }
}
