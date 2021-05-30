package xyz.pokecord.bot.core.managers.database.repositories

import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.set
import org.litote.kmongo.setTo
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.Config
import xyz.pokecord.bot.core.managers.database.models.PaypalCredentials

class ConfigRepository(database: Database, private val collection: CoroutineCollection<Config>) : Repository(database) {
  suspend fun getConfig(): Config {
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
}
