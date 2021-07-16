package xyz.pokecord.bot.core.managers.database.repositories

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import org.litote.kmongo.set
import org.litote.kmongo.setTo
import org.redisson.api.RMapCacheAsync
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.Guild
import xyz.pokecord.bot.core.managers.database.models.User
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.extensions.awaitSuspending
import net.dv8tion.jda.api.entities.Guild as JDAGuild

class GuildRepository(
  database: Database,
  private val collection: CoroutineCollection<Guild>,
  private val cacheMap: RMapCacheAsync<String, String>
) : Repository(database) {
  private suspend fun getCacheGuild(guildId: String): Guild? {
    val json = cacheMap.getAsync(guildId).awaitSuspending() ?: return null
    return Json.decodeFromString(json)
  }

  private suspend fun setCacheGuild(guildId: String, guild: Guild) {
    if (guild._isNew && !guild.isDefault) {
      guild._isNew = false
      collection.insertOne(guild)
    }
    cacheMap.putAsync(guildId, Json.encodeToString(guild)).awaitSuspending()
  }

  suspend fun getGuild(jdaGuild: JDAGuild): Guild {
    var guild = getCacheGuild(jdaGuild.id)
    if (guild == null) {
      guild = collection.findOne(Guild::id eq jdaGuild.id)
      if (guild == null) {
        guild = Guild(jdaGuild.id, _isNew = true)
      } else setCacheGuild(jdaGuild.id, guild)
    }
    return guild
  }

  suspend fun setPrefix(guildData: Guild, prefix: String?): Guild {
    guildData.prefix = prefix
    collection.updateOne(Guild::id eq guildData.id, set(Guild::prefix setTo prefix))
    setCacheGuild(guildData.id, guildData)
    return guildData
  }

  suspend fun toggleSilence(guildData: Guild) {
    guildData.levelUpMessagesSilenced = !guildData.levelUpMessagesSilenced
    collection.updateOne(User::id eq guildData.id, set(User::progressPrivate setTo guildData.levelUpMessagesSilenced))
    setCacheGuild(guildData.id, guildData)
  }
}
