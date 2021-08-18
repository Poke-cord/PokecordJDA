package xyz.pokecord.bot.modules.staff.commands

import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.ShardStatus
import xyz.pokecord.bot.modules.staff.StaffCommand
import xyz.pokecord.bot.utils.EmbedPaginator
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.extensions.awaitSuspending
import xyz.pokecord.bot.utils.extensions.humanizeMs
import kotlin.math.ceil

class ShardsCommand : StaffCommand() {
  override val name = "Shards"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(optional = true) page: Int?
  ) {
    val paginatorIndex = (page ?: 1) - 1
    val shardStatusSet = module.bot.cache.shardStatusMap.readAllValuesAsync().awaitSuspending()
      .map { json -> Json.decodeFromString<ShardStatus>(json) }.sortedBy { it.id }

    val now = System.currentTimeMillis()
    val possiblyDeadHosts = shardStatusSet.filter { now - it.updatedAt >= ALIVE_TIMEOUT_MS }.map { it.hostname }
    val guildCount = shardStatusSet.sumOf { it.guildCacheSize }

    EmbedPaginator(context, ceil(shardStatusSet.size / 10.0).toInt(), {
      val startingIndex = it * 10
      val items = shardStatusSet.drop(startingIndex).take(10)
      val shardList = items.joinToString("\n") { status ->
        "${status.id}/${status.count} - ${status.gatewayPing}ms - ${status.hostname} - ${
          (System.currentTimeMillis() - status.updatedAt).humanizeMs()
        } - ${status.guildCacheSize}"
      }
      context.embedTemplates.normal(
        "$shardList\n\nCurrent Shard: ${context.jda.shardInfo.shardId}\nGuild Count: $guildCount\n\nPossibly Dead Hosts: ${
          possiblyDeadHosts.joinToString(", ").ifEmpty { "None" }
        }",
        "Shard Status"
      )
    }, paginatorIndex).start()
  }

  companion object {
    const val ALIVE_TIMEOUT_MS = 300_000
  }
}
