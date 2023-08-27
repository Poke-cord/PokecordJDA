package xyz.pokecord.bot.modules.economy.commands

import net.dv8tion.jda.api.entities.MessageEmbed
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.ItemData
import xyz.pokecord.bot.core.structures.pokemon.items.ItemFactory
import xyz.pokecord.bot.utils.EmbedPaginator
import kotlin.math.ceil

class ShopCommand : Command() {
  override val name = "Shop"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(optional = true) page: Int?,
    @Argument(optional = true, consumeRest = true) search: String?
  ) {
    if (!context.hasStarted(true)) return

    val filteredItems = ItemData.items.filter {
      it.cost > 0 && ItemFactory.items.contains(it.id) && (if (search != null) it.name.contains(
        search,
        true
      ) || it.identifier.contains(search, true) else true)
    }

    if (filteredItems.isEmpty()) {
      context.reply(
        context.embedTemplates.error(
          context.translate("misc.errors.noSearchResults")
        ).build()
      ).queue()
      return
    }

    val userData = context.getUserData()

    val multiplier = userData.getShopDiscount()

    val embedPaginator = EmbedPaginator(context, ceil(filteredItems.size / 9.0).toInt(), {
      val fields = filteredItems.drop(it * 9).take(9).map { itemData ->
        val cost = when {
          itemData.usesGems -> context.translate(
            "modules.economy.commands.shop.texts.gemsCost",
            "cost" to context.translator.numberFormat(itemData.cost)
          )
          itemData.usesTokens -> context.translate(
            "modules.economy.commands.shop.texts.tokensCost",
            "cost" to context.translator.numberFormat(itemData.cost)
          )
          itemData.cost > 0 -> context.translate(
            "modules.economy.commands.shop.texts.creditsCost",
            "cost" to context.translator.numberFormat(itemData.cost.toDouble() * multiplier)
          )
          else -> context.translate("modules.economy.commands.shop.texts.cantBePurchased")
        }
        MessageEmbed.Field(
          itemData.name,
          context.translate(
            "modules.economy.commands.shop.texts.cost",
            "cost" to cost
          ),
          true
        )
      }
      val embed = context.embedTemplates.menu(
        context.translate(
          "modules.economy.commands.shop.embed.description",
          mapOf(
            "gems" to context.translator.numberFormat(userData.gems),
            "credits" to context.translator.numberFormat(userData.credits),
            "tokens" to context.translator.numberFormat(userData.tokens)
          )
        ),
        context.translate("modules.economy.commands.shop.embed.title")
      )//.setAuthor(
//        context.translate("modules.economy.commands.shop.texts.pokecordShop"),
//        null,
//        "https://cdn.discordapp.com/attachments/719524226708668446/720936022539632680/unknown-removebg-preview.png"
//      )
      fields::forEach { field ->
        embed.addField(field)
      }
      embed
    }, (page ?: 1) - 1)
    embedPaginator.start()
  }
}
