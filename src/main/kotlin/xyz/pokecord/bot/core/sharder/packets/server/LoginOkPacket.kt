package xyz.pokecord.bot.core.sharder.packets.server

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.pokecord.bot.core.sharder.packets.Packet
import xyz.pokecord.bot.core.sharder.packets.client.KeepAliveRequestPacket

class LoginOkPacket : Packet() {
  override val id: Short = 20104

  override suspend fun processReceive() {
    session.client.logger.info("Login successful! Starting sending keep alive requests...")
    val keepAliveRequestPacket = KeepAliveRequestPacket()
    session.keepAliveSenderJob = GlobalScope.launch {
      while (true) {
        launch {
          session.sendPacket(keepAliveRequestPacket)
          session.client.logger.debug("Keep alive request sent!")
        }
        delay(5000)
      }
    }
  }
}
