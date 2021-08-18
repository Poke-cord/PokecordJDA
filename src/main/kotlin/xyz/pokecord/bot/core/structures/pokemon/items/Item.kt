package xyz.pokecord.bot.core.structures.pokemon.items

import net.dv8tion.jda.api.EmbedBuilder
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.pokemon.ItemData

abstract class Item(
  val id: Int
) {
  data class UsageResult(
    val consumeItem: Boolean,
    val responseEmbed: EmbedBuilder
  )

  val data by lazy { ItemData.getById(id)!! }

  abstract suspend fun use(context: ICommandContext, args: List<String>): UsageResult
}
