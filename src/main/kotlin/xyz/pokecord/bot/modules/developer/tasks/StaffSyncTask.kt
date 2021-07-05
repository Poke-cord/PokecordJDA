package xyz.pokecord.bot.modules.developer.tasks

import kotlinx.serialization.encodeToString
import xyz.pokecord.bot.core.structures.discord.base.Task
import xyz.pokecord.bot.utils.CachedStaffMember
import xyz.pokecord.bot.utils.Config
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.extensions.awaitSuspending

class StaffSyncTask : Task() {
  override val interval = 60_000L
  override val name = "StaffSync"

  override suspend fun execute() {
    module.bot.jda.getGuildById(Config.mainServer)?.let { guild ->
      val cachedStaffMemberObjects = guild.members.mapNotNull { member ->
        return@mapNotNull member.roles.filter { Config.Roles.ids.contains(it.id) }.minByOrNull { it.position }
          ?.let { role ->
            CachedStaffMember(
              member.user.asTag,
              member.user.effectiveAvatarUrl,
              role.name,
              role.position
            )
          }
      }
      module.bot.cache.staffMembersSet.deleteAsync().awaitSuspending()
      module.bot.cache.staffMembersSet.addAllAsync(cachedStaffMemberObjects.map { Json.encodeToString(it) })
    }
  }
}
