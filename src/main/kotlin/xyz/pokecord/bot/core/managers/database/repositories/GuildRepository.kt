package xyz.pokecord.bot.core.managers.database.repositories

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.litote.kmongo.ascendingIndex
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import org.litote.kmongo.set
import org.litote.kmongo.setTo
import org.redisson.api.RMapCacheAsync
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.Guild
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.extensions.awaitSuspending
import net.dv8tion.jda.api.entities.Guild as JDAGuild

class GuildRepository(
  database: Database,
  private val collection: CoroutineCollection<Guild>,
  private val cacheMap: RMapCacheAsync<String, String>
) : Repository(database) {
  override suspend fun createIndexes() {
    collection.createIndex(ascendingIndex(Guild::id))
  }

  private suspend fun getCacheGuild(guildId: String): Guild? {
    val json = cacheMap.getAsync(guildId).awaitSuspending() ?: return null
    return Json.decodeFromString(json)
  }

  private suspend fun setCacheGuild(guild: Guild) {
    if (guild._isNew && !guild.isDefault) {
      guild._isNew = false
      collection.insertOne(guild)
    }
    cacheMap.putAsync(guild.id, Json.encodeToString(guild)).awaitSuspending()
  }

  suspend fun getGuild(jdaGuild: JDAGuild): Guild {
    var guild = getCacheGuild(jdaGuild.id)
    if (guild == null) {
      guild = collection.findOne(Guild::id eq jdaGuild.id)
      if (guild == null) {
        guild = Guild(jdaGuild.id, _isNew = true)
      } else setCacheGuild(guild)
    }
    return guild
  }

  suspend fun setPrefix(guildData: Guild, prefix: String?): Guild {
    guildData.prefix = prefix
    collection.updateOne(Guild::id eq guildData.id, set(Guild::prefix setTo prefix))
    setCacheGuild(guildData)
    return guildData
  }

  suspend fun toggleSilence(guildData: Guild) {
    guildData.levelUpMessagesSilenced = !guildData.levelUpMessagesSilenced
    collection.updateOne(
      Guild::id eq guildData.id,
      set(Guild::levelUpMessagesSilenced setTo guildData.levelUpMessagesSilenced)
    )
    setCacheGuild(guildData)
  }

  suspend fun setLevelUpMessageChannel(guildData: Guild, channelId: String?) {
    guildData.levelUpChannelId = channelId
    collection.updateOne(Guild::id eq guildData.id, set(Guild::levelUpChannelId setTo guildData.levelUpChannelId))
    setCacheGuild(guildData)
  }

  suspend fun clearCache() {
    cacheMap.deleteAsync().awaitSuspending()
  }
}
