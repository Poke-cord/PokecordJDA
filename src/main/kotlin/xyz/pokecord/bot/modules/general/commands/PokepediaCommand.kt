package xyz.pokecord.bot.modules.general.commands

import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.modules.general.commands.pokepedia.*

class PokepediaCommand : ParentCommand() {
  override val childCommands =
    mutableListOf(PokepediaIntroCommand, PokepediaTutorialCommand)
  override val name = "Pokepedia"

  override var aliases = arrayOf("pkp", "pkpd", "ppd", "encyclopedia", "pedia")
}