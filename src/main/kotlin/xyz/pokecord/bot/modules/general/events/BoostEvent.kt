package xyz.pokecord.bot.modules.general.events

import net.dv8tion.jda.api.entities.MessageType
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import xyz.pokecord.bot.core.structures.discord.base.Event
import xyz.pokecord.bot.core.structures.pokemon.items.RedeemItem
import xyz.pokecord.bot.utils.Config
import kotlin.random.Random

object BoostEvent: Event() {
  override val name = "BoostRewards"

  @Handler
  suspend fun onBoost(event: GuildMessageReceivedEvent) {
    if(event.message.type == MessageType.GUILD_MEMBER_BOOST && event.guild.id == Config.mainServer) {
      val randomRedeem = RedeemItem.redeemMap.values.random()
      val userData = module.bot.database.userRepository.getUser(event.member!!.user)
      if(userData.lastBoostAt == null || userData.lastBoostAt!! < System.currentTimeMillis() - Config.boostCooldown) {
        module.bot.database.userRepository.setLastBoostTime(userData)
        module.bot.database.userRepository.addInventoryItem(userData.id, randomRedeem.data.id, 1)
        module.bot.httpServer.sendBoostNotification(event.member!!.id, randomRedeem.data.name)
      }
    }
  }
}