package xyz.pokecord.bot.modules.developer.commands

import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.store.packages.RolesPackage
import xyz.pokecord.bot.modules.developer.DeveloperCommand

object GiveRoleCommand : DeveloperCommand() {
  override val name = "GiveRole"
  override var aliases = arrayOf("grl")

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
      context.reply(context.embedTemplates.error("No dono role or perks found with the given role.").build()).queue()
      return
    }

    val userData = context.bot.database.userRepository.getUser(user)
    RolesPackage.giveReward(context.bot, userData, roleItem)

    context.reply(
      context.embedTemplates.normal("${user.asMention} has been granted the ${role.asMention} role and its rewards.").build()
    ).queue()
  }
}
