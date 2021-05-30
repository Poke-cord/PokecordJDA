package xyz.pokecord.bot.modules.general.commands

import xyz.pokecord.bot.core.structures.discord.Command
import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext
import xyz.pokecord.bot.utils.EmbedPaginator

class HelpCommand : Command() {
  override val name = "Help"

  @Executor
  suspend fun execute(
    context: MessageReceivedContext,
    @Argument(optional = true) page: Int?,
    @Argument(name = "command/module", optional = true) commandOrModuleName: String?,
    @Argument(name = "command", optional = true) commandName: String?,
    @Argument(name = "sub command", optional = true) subCommandName: String?,
  ) {
    val prefix = context.getPrefix()
    if (commandOrModuleName != null) {
      val module = module.bot.modules[commandOrModuleName.toLowerCase()]
      if (module != null) {
        if (commandName != null) {
          if (subCommandName != null) {
            val command =
              module.commandMap["${commandName.toLowerCase()}.${subCommandName.toLowerCase()}"]

            if (command != null) {
              val helpEmbed = this.module.bot.getHelpEmbed(context, command)
              if (helpEmbed != null) {
                context.reply(helpEmbed.build()).queue()
                return
              }
            }
            context.reply(
              context.embedTemplates.error("No command(s) found or you don't have access to the command(s).").build()
            ).queue()
          }
          val command =
            module.commandMap[commandName.toLowerCase()]

          if (command != null) {
            val helpEmbed = this.module.bot.getHelpEmbed(context, command)
            if (helpEmbed != null) {
              context.reply(helpEmbed.build()).queue()
              return
            }
          }
          context.reply(
            context.embedTemplates.error("No command(s) found or you don't have access to the command(s).").build()
          ).queue()
        } else {
          context.reply(module.bot.getHelpEmbed(context, module, prefix).build()).queue()
        }
      } else {
        val commands = this.module.bot.modules.mapNotNull {
          it.value.commandMap["${commandOrModuleName.toLowerCase()}.${commandName?.toLowerCase()}"]
            ?: it.value.commandMap[commandOrModuleName.toLowerCase()]
        }
        if (commands.isNotEmpty()) {
          val helpEmbeds = this.module.bot.getHelpEmbeds(context, commands)
          if (helpEmbeds.isNotEmpty()) {
            val paginator = EmbedPaginator(context, helpEmbeds.size, {
              helpEmbeds[it]
            }, if (page == null) 0 else page - 1)
            paginator.start()
            return
          }
        }
        context.reply(
          context.embedTemplates.error("No command(s) found or you don't have access to the command(s).").build()
        ).queue()
      }
    } else {
      val helpEmbeds = module.bot.getHelpEmbeds(context, prefix)
      val paginator = EmbedPaginator(context, helpEmbeds.size, {
        helpEmbeds[it]
      }, if (page == null) 0 else page - 1)
      paginator.start()
    }
  }
}
