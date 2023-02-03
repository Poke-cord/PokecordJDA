package xyz.pokecord.bot.modules.staff.commands

import net.dv8tion.jda.api.entities.User
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.pokemon.items.RedeemItem
import xyz.pokecord.bot.core.structures.store.packages.RolesPackage
import xyz.pokecord.bot.modules.staff.StaffCommand

object GiveRedeemCommand : StaffCommand() {
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
      context.reply(context.embedTemplates.error("no redeem found with that name").build()).queue()
      return
    }

    val userData = context.bot.database.userRepository.getUser(user)
    context.bot.database.userRepository.addInventoryItem(userData.id, redeemItem.id, 1)

    context.reply(
      context.embedTemplates.normal("${user.asMention} has been given the ${redeemItem.data.name}").build()
    ).queue()
  }
}
