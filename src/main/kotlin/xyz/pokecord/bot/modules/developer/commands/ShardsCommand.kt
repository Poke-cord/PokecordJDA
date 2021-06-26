package xyz.pokecord.bot.modules.developer.commands

import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.ShardStatus
import xyz.pokecord.bot.modules.developer.DeveloperCommand
import xyz.pokecord.bot.utils.EmbedPaginator
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.extensions.awaitSuspending
import xyz.pokecord.bot.utils.extensions.humanizeMs
import kotlin.math.ceil

class ShardsCommand : DeveloperCommand() {
  override val name = "Shards"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(optional = true) page: Int?
  ) {
    val paginatorIndex = (page ?: 1) - 1
    val shardStatusSet = module.bot.cache.shardStatusMap.readAllValuesAsync().awaitSuspending()
      .map { json -> Json.decodeFromString<ShardStatus>(json) }.sortedBy { it.id }

    EmbedPaginator(context, ceil(shardStatusSet.size / 10.0).toInt(), {
      val startingIndex = it * 10
      val items = shardStatusSet.drop(startingIndex).take(10)
      context.embedTemplates.normal(
        items.joinToString("\n") { status ->
          "${status.id}/${status.count} - ${status.gatewayPing}ms - ${status.hostname} - ${
            (System.currentTimeMillis() - status.updatedAt).humanizeMs()
          } - ${status.guildCacheSize}"
        },
        "Shard Status"
      )
    }, paginatorIndex).start()
  }
}
