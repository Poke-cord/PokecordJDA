package xyz.pokecord.bot.core.managers.database.repositories

import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.Config
import xyz.pokecord.bot.core.managers.database.models.PaypalCredentials

class ConfigRepository(database: Database, private val collection: CoroutineCollection<Config>) : Repository(database) {
  override suspend fun createIndexes() {
    // No need of index here
  }

  private suspend fun getConfig(): Config {
    var config = collection.findOne()
    if (config == null) {
      config = Config()
      collection.insertOne(config)
    }
    return config
  }

  suspend fun getPaypalCredentials(): PaypalCredentials? {
    val config = getConfig()
    return config.paypalCredentials
  }

  suspend fun setPaypalCredentials(paypalCredentials: PaypalCredentials) {
    collection.updateOne(EMPTY_BSON, set(Config::paypalCredentials setTo paypalCredentials))
  }

  suspend fun addSusBlacklist(id: String) {
    collection.updateOne(EMPTY_BSON, addToSet(Config::susBlacklistIds, id))
  }
  suspend fun removeSusBlacklist(id: String) {
    collection.updateOne(EMPTY_BSON, pull(Config::susBlacklistIds, id))
  }
  suspend fun getSusBlacklistIds(): List<String> {
    return getConfig().susBlacklistIds
  }
}
