package xyz.pokecord.bot.core.sharder.packets.client

import xyz.pokecord.bot.core.sharder.packets.Packet

class KeepAliveRequestPacket : Packet() {
  override val id: Short = 10108
}
