package xyz.pokecord.bot.modules.general.events

import net.dv8tion.jda.api.events.ReadyEvent
import xyz.pokecord.bot.core.structures.discord.Event

class ReadyEvent : Event() {
  override val name = "Ready"

  @Handler
  fun onReady(event: ReadyEvent) {
    module.bot.logger.info("Logged in as ${event.jda.selfUser.asTag}!")
    module.bot.updatePresence()
  }
}
