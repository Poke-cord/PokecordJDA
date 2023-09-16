package xyz.pokecord.bot.modules.developer.commands

import net.dv8tion.jda.api.entities.User
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.pokemon.items.RedeemItem
import xyz.pokecord.bot.modules.developer.DeveloperCommand

object GiveRedeemCommand : DeveloperCommand() {
  override val name = "GiveRedeem"
  override var aliases = arrayOf("grd")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument user: User?,
    @Argument redeemName: String?,
  ) {
    if (user == null) {
      context.reply(context.embedTemplates.error("gib user pls").build()).queue()
      return
    }
    if (redeemName == null) {
      context.reply(context.embedTemplates.error("gib redeem name pls").build()).queue()
      return
    }

    val redeemItem = RedeemItem.redeemMap.values.find {
      it.data.name.contains(redeemName, true)
    }

    if (redeemItem == null) {
      context.reply(context.embedTemplates.error("No redeem found with the given name.").build()).queue()
      return
    }

    val userData = context.bot.database.userRepository.getUser(user)
    context.bot.database.userRepository.addInventoryItem(userData.id, redeemItem.id, 1)

    context.reply(
      context.embedTemplates.normal("${user.asMention} has been granted a ${redeemItem.data.name}.").build()
    ).queue()
  }
}
