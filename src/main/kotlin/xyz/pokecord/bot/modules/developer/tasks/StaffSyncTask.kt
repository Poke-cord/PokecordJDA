package xyz.pokecord.bot.modules.developer.tasks

import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import xyz.pokecord.bot.core.structures.discord.base.Task
import xyz.pokecord.bot.utils.CachedStaffMember
import xyz.pokecord.bot.utils.Config
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.extensions.awaitSuspending

class StaffSyncTask : Task() {
  override val interval = 300_000L
  override val name = "StaffSync"

  override suspend fun execute() {
    try {
      val mainServerShardId = ((Config.mainServer.toLong() shr 22) % module.bot.shardManager.shardsTotal).toInt()
      if (module.bot.shardManager.getShardById(mainServerShardId) != null) {
        val guildRoles =
          module.bot.discordRestClient.getGuildRoles(Config.mainServer).filter { Config.Roles.ids.contains(it.id) }
            .sortedByDescending { it.position }
        val staffMemberIds = module.bot.cache.staffMemberIds.readAllAsync().awaitSuspending()
        val cachedStaffMemberObjects = staffMemberIds.mapNotNull { staffMemberId ->
          val guildMember = module.bot.discordRestClient.getGuildMember(Config.mainServer, staffMemberId)
          val role = guildRoles.find { guildMember.roles.contains(it.id) } ?: return@mapNotNull null
          val avatarUrl =
            if (guildMember.user.avatar == null) "https://cdn.discordapp.com/embed/avatars/${guildMember.user.discriminator.toInt() % 5}.png"
            else "https://cdn.discordapp.com/avatars/${guildMember.user.id}/${guildMember.user.avatar}.${
              if (guildMember.user.avatar.startsWith(
                  "a_"
                )
              ) "gif" else "png"
            }"

          // TODO: implement rate limit handling
          delay(1000)
          CachedStaffMember(
            guildMember.user.username,
            guildMember.user.discriminator,
            avatarUrl,
            role.name,
            role.position
          )
        }
        module.bot.cache.staffMembersSet.deleteAsync().awaitSuspending()
        module.bot.cache.staffMembersSet.addAllAsync(cachedStaffMemberObjects.map { Json.encodeToString(it) })
      }
    } catch (e: Throwable) {
      e.printStackTrace()
    }
  }
}
