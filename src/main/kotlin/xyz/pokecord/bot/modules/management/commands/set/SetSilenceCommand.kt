package xyz.pokecord.bot.modules.management.commands.set

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.Permission
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object SetSilenceCommand : Command() {
  override val name = "Silence"

  @Executor
  suspend fun execute(context: ICommandContext) {
    if (!context.isFromGuild) {
      context.reply(
        context.translate("misc.errors.serverOnlyCommand")
      ).queue()
      return
    }

    if (!context.guild!!.retrieveMember(context.author).await().hasPermission(Permission.ADMINISTRATOR)) {
      // TODO: say you're not admin PROPERLY
      context.reply(
        context.embedTemplates.error("Please ask a server administrator to use this command instead.").build()
      ).queue()
      return
    }

    val guildData = context.getGuildData()!!
    module.bot.database.guildRepository.toggleSilence(guildData)
    context.reply(
      context.embedTemplates.normal(
        context.translate(if (!guildData.levelUpMessagesSilenced) "modules.general.commands.set.silence.enabled" else "modules.general.commands.set.silence.disabled"),
        context.translate("modules.general.commands.set.silence.title")
      ).build()
    ).queue()
  }
}