package xyz.pokecord.bot.modules.staff.commands

import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.store.packages.RolesPackage
import xyz.pokecord.bot.modules.staff.StaffCommand

class GiveRedeemCommand : StaffCommand() {
  override val name = "GiveRole"
  override var aliases = arrayOf("gr")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument user: User?,
    @Argument role: Role?,
  ) {
    if (user == null) {
      context.reply(context.embedTemplates.error("gib user pls").build()).queue()
      return
    }
    if (role == null) {
      context.reply(context.embedTemplates.error("gib role pls").build()).queue()
      return
    }

    val roleItem = RolesPackage.items.find {
      it is RolesPackage.RoleItem && it.roleId == role.id
    }

    if (roleItem == null) {
      context.reply(context.embedTemplates.error("role is not a valid package").build()).queue()
      return
    }

    val userData = context.bot.database.userRepository.getUser(user)
    RolesPackage.giveReward(context.bot, userData, roleItem)

    context.reply(
      context.embedTemplates.normal("${user.asMention} has been given the rewards of role ${role.asMention}").build()
    ).queue()
  }
}
