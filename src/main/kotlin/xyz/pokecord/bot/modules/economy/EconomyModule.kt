package xyz.pokecord.bot.modules.economy

import net.dv8tion.jda.api.hooks.EventListener
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.modules.economy.commands.BuyCommand
import xyz.pokecord.bot.modules.economy.commands.ShopCommand
import xyz.pokecord.bot.modules.economy.commands.StoreCommand

class EconomyModule(bot: Bot) : Module(
  bot,
  arrayOf(
    BuyCommand(),
    ShopCommand(),
    StoreCommand()
  )
), EventListener {
  override val name = "Economy"
}
