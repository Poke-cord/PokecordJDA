package xyz.pokecord.bot.modules.profile.commands.profile

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command

object ProfileChecklistCommand : Command() {
  override val name = "Checklist"

  override var aliases = arrayOf("cl","task","tasks")

  @Executor
  suspend fun execute(
    context: ICommandContext
  ) {

// vars/vals go here

    context.reply(
      context.embedTemplates.normal(
        context.translate("modules.profile.commands.profile.embed.checklist.description"),
        context.translate("modules.profile.commands.profile.embed.checklist.title",
            "user" to context.author.asTag
        )
      )//.setFooter(context.translate("modules.profile.commands.profile.embed.info.footer"))
        .addField(
          context.translate("modules.profile.commands.profile.embed.checklist.fields.firstTime.key"),
          context.translate("modules.profile.commands.profile.embed.checklist.fields.firstTime.value"),
          false
        )
        .addField(
          context.translate("modules.profile.commands.profile.embed.checklist.fields.commands.key"),
          context.translate("modules.profile.commands.profile.embed.checklist.fields.commands.value"),
          false
        )
        .build()
    ).queue()
  }
}