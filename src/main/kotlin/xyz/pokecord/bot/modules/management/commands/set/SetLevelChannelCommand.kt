package xyz.pokecord.bot.modules.management.commands.set

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object SetLevelChannelCommand : Command() {
  override val name = "Level"
  override var aliases = arrayOf("lvl", "l")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument textChannel: TextChannel?
  ) {
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

    if (textChannel == null) {
      context.bot.database.guildRepository.setLevelUpMessageChannel(context.getGuildData()!!, null)

      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.general.commands.set.level.description.removed",
          ),
          context.translate(
            "modules.general.commands.set.level.title.removed",
          ),
        ).build()
      ).queue()
    } else {
      context.bot.database.guildRepository.setLevelUpMessageChannel(context.getGuildData()!!, textChannel.id)

      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.general.commands.set.level.description.updated",
            "channel" to textChannel.asMention
          ),
          context.translate(
            "modules.general.commands.set.level.title.updated",
          ),
        ).build()
      ).queue()
    }
  }
}