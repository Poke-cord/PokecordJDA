package xyz.pokecord.bot.modules.developer.commands

import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.MessageCommandContext
import xyz.pokecord.bot.core.structures.discord.ShardStatus
import xyz.pokecord.bot.modules.developer.DeveloperCommand
import xyz.pokecord.bot.utils.EmbedPaginator
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.extensions.awaitSuspending
import xyz.pokecord.bot.utils.extensions.humanizeMs
import kotlin.math.ceil

class ShardsCommand : DeveloperCommand() {
  override val name = "Shards"

//  private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(optional = true) page: Int?
  ) {
    val paginatorIndex = (page ?: 1) - 1
    val shardStatusSet = module.bot.cache.shardStatusMap.readAllValuesAsync().awaitSuspending().toList()
    EmbedPaginator(context, ceil(shardStatusSet.size / 10.0).toInt(), {
      val startingIndex = it * 10
      val items = shardStatusSet.drop(startingIndex).take(10)
      context.embedTemplates.normal(
        items.joinToString("\n") { statusJson ->
          val status = Json.decodeFromString<ShardStatus>(statusJson)
          "${status.id}/${status.count} - ${status.gatewayPing}ms - ${status.hostname} - ${
            (System.currentTimeMillis() - status.updatedAt).humanizeMs()
//            Instant.ofEpochMilli(
//              status.updatedAt
//            ).atZone(ZoneId.systemDefault()).toLocalTime().format(timeFormatter)
          }"
        },
        "Shard Status"
      )
    }, paginatorIndex).start()
  }
}
