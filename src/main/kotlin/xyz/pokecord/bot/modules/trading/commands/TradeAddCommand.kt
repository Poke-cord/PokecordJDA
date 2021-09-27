package xyz.pokecord.bot.modules.trading.commands

import xyz.pokecord.bot.core.structures.discord.base.ParentCommand

object TradeAddCommand : ParentCommand() {
  override val childCommands = mutableListOf(TradeAddCreditsCommand, TradeAddPokemonCommand)

  override val name = "Add"

//  @Executor
//  suspend fun execute(
//    context: ICommandContext
//  ) {
//    context.reply(
//      context.embedTemplates.error(
//        context.translate("modules.trading.commands.add.errors.noTypeProvided")
//      ).build()
//    ).queue()
//  }
}
