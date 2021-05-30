package xyz.pokecord.bot.core.sharder.packets.server

import xyz.pokecord.bot.core.sharder.packets.Packet
import xyz.pokecord.bot.core.sharder.packets.client.LoginRequestPacket

class HandshakeOkPacket : Packet() {
  override val id: Short = 20100

  override suspend fun processReceive() {
    session.client.logger.info("Handshake successful!")
    val loginRequestPacket = LoginRequestPacket()
    loginRequestPacket.botToken = System.getenv("BOT_TOKEN")
    session.sendPacket(loginRequestPacket)
  }
}
