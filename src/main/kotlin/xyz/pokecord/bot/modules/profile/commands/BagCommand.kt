package xyz.pokecord.bot.modules.profile.commands

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.ItemData

class BagCommand : Command() {
  override val name = "Bag"

  override var aliases = arrayOf("backpack", "bp", "inventory", "bal")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument user: User?
  ) {
    if (!context.hasStarted(true)) return
    val targetUser = user ?: context.author
    val checkingSelf = user == null

    val userData = if (checkingSelf) context.getUserData() else module.bot.database.userRepository.getUser(targetUser)
    // TODO: moderator check
    if (userData.progressPrivate && !checkingSelf) {
      context.reply(context.embedTemplates.progressPrivate(targetUser).build()).queue()
      return
    }

    val items = module.bot.database.userRepository.getInventoryItems(context.author.id).mapNotNull {
      val itemData = ItemData.getById(it.id) ?: return@mapNotNull null
      MessageEmbed.Field(itemData.name, context.translator.numberFormat(it.amount), true)
    }

    if (items.isEmpty()) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.profile.commands.bag.errors.noItems")
        ).build()
      ).queue()
      return
    }

    val embed = context.embedTemplates.normal(
      context.translate(
        "modules.profile.commands.bag.description",
        mapOf(
          "credits" to context.translator.numberFormat(userData.credits),
          "gems" to context.translator.numberFormat(userData.gems),
          "tokens" to context.translator.numberFormat(userData.tokens)
        )
      ),
      context.translate("modules.profile.commands.bag.title", "user" to context.author.asTag)
    )
    items.forEach(embed::addField)
    context.reply(embed.build()).queue()
  }
}
