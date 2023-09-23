package xyz.pokecord.bot.modules.general.commands.set

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.Permission
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
//import xyz.pokecord.bot.modules.staff.StaffCommand

object SetPrefixCommand : Command() {
  override val name = "Prefix"

  override var requiredUserPermissions = arrayOf(Permission.ADMINISTRATOR)
  override var enabled = false

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument prefix: String?
  ) {
    if (!context.isFromGuild) {
      context.reply(
        context.translate("misc.texts.serverOnlyCommand")
      ).queue()
      return
    }

    if (!context.guild!!.retrieveMember(context.author).await().hasPermission(Permission.ADMINISTRATOR)) {
      // TODO: say you're not admin PROPERLY
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.general.commands.set.prefix.noAdmin")
        ).build()
      ).queue()
      return
    }

    if (prefix == null) {
      context.reply(
        context.embedTemplates.normal(
          context.translate("modules.general.commands.set.prefix.current", "prefix" to context.getPrefix())
        ).build()
      ).queue()
      return
    }

    if (prefix.length > 10) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.general.commands.set.prefix.tooLarge")
        ).build()
      ).queue()
      return
    }

    module.bot.database.guildRepository.setPrefix(context.getGuildData()!!, prefix)
    context.reply(
      context.embedTemplates.normal(
        context.translate("modules.general.commands.set.prefix.updated", "prefix" to prefix)
      ).build()
    ).queue()
  }
}