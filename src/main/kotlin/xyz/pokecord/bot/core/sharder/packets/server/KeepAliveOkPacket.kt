package xyz.pokecord.bot.core.sharder.packets.server

import xyz.pokecord.bot.core.sharder.packets.Packet

class KeepAliveOkPacket : Packet() {
  override val id: Short = 20108

  override suspend fun processReceive() {
    session.lastHeartbeatAt = System.currentTimeMillis()
  }
}
