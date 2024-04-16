package xyz.pokecord.bot.modules.profile.commands

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.ItemData
import xyz.pokecord.bot.utils.EmbedPaginator
import kotlin.math.ceil

class BagCommand : Command() {
  override val name = "Bag"
  override var aliases = arrayOf("bal", "bp", "inv", "items", "balance", "backpack", "inventory")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument user: User?
  ) {
    if (!context.hasStarted(true)) return
    val targetUser = user ?: context.author
    val checkingSelf = user == null

    val userData = if (checkingSelf) context.getUserData() else module.bot.database.userRepository.getUser(targetUser)
    if (!context.isStaff() && userData.progressPrivate && !checkingSelf) {
      context.reply(context.embedTemplates.progressPrivate(targetUser).build()).queue()
      return
    }

    val items = module.bot.database.userRepository.getInventoryItems(targetUser.id).mapNotNull {
      val itemData = ItemData.getById(it.id) ?: return@mapNotNull null
      MessageEmbed.Field(itemData.name, context.translator.numberFormat(it.amount), true)
    }

    if (items.isEmpty()) {
      context.reply(
        context.embedTemplates.menu(
          context.translate(
            "modules.profile.commands.bag.noItemsDescription",
            mapOf(
              "credits" to context.translator.numberFormat(userData.credits),
              "gems" to context.translator.numberFormat(userData.gems),
              "tokens" to context.translator.numberFormat(userData.tokens)
            )
          ),
          context.translate("modules.profile.commands.bag.title", "user" to targetUser.asTag)
        ).build()
      ).queue()
      return
    }

    val pageCount = ceil((items.size.toDouble() / 9)).toInt()
    EmbedPaginator(
      context,
      pageCount,
      {
        val embed = context.embedTemplates.menu(
          context.translate(
            "modules.profile.commands.bag.description",
            mapOf(
              "credits" to context.translator.numberFormat(userData.credits),
              "gems" to context.translator.numberFormat(userData.gems),
              "tokens" to context.translator.numberFormat(userData.tokens)
            )
          ),
          context.translate("modules.profile.commands.bag.title", "user" to targetUser.asTag)
        )
        items.drop(it * 9).take(9).forEach(embed::addField)
        embed
      }
    ).start()
  }
}
