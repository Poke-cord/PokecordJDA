package xyz.pokecord.bot.modules.general.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.EmbedPaginator

class HelpCommand : Command() {
  override val name = "Help"

  override var aliases = arrayOf("h")

  private suspend fun getCommands(
    context: ICommandContext,
    page: Int?,
    commandOrModuleName: String?,
    commandName: String?
  ) {
    val commands = this.module.bot.modules.mapNotNull {
      if(commandOrModuleName == null) return@mapNotNull null
      it.value.commandMap["${commandOrModuleName.lowercase()}.${commandName?.lowercase()}"]
        ?: it.value.commandMap[commandOrModuleName.lowercase()]
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

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(optional = true) page: Int?,
    @Argument(name = "module", optional = true) commandOrModuleName: String?,
    @Argument(name = "command", optional = true) commandName: String?,
    @Argument(name = "subcommand", optional = true) subCommandName: String?,
  ) {
    val prefix = context.getPrefix()
    if (commandOrModuleName != null) {
      val module = module.bot.modules[commandOrModuleName.lowercase()]
      if (module != null) {
        if (commandName != null) {
          if (subCommandName != null) {
            val command =
              module.commandMap["${commandName.lowercase()}.${subCommandName.lowercase()}"]

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
            module.commandMap[commandName.lowercase()]

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
          val embed = module.bot.getHelpEmbed(context, module, prefix)
          context.reply(
            (embed
              ?: context.embedTemplates.error("No command(s) found or you don't have access to the command(s).")).build()
          ).queue()
        }
      } else {
        getCommands(context, page, commandOrModuleName, commandName)
        return
      }
    } else {
      if (commandName != null) {
        getCommands(context, page, commandName, subCommandName)
      } else {
        val helpEmbeds = module.bot.getHelpEmbeds(context, prefix)
        val paginator = EmbedPaginator(context, helpEmbeds.size, {
          helpEmbeds[it]
        }, if (page == null) 0 else page - 1)
        paginator.start()
      }
    }
  }
}
