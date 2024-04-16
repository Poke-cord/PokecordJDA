package xyz.pokecord.bot.modules.trainer

import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.trainer.commands.*

class TrainerModule(bot: Bot) : Module(
  bot,
  arrayOf(
    CatchCommand(),
    GiftCommand,
    HintCommand(),
    PickCommand(),
    RewardsCommand(),
    StartCommand(),
  )
) {
  override val name = "Trainer"
}
