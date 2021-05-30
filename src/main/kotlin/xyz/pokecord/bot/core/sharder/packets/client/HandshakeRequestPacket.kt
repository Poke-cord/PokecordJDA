package xyz.pokecord.bot.core.sharder.packets.client

import xyz.pokecord.bot.core.sharder.packets.Packet

class HandshakeRequestPacket : Packet() {
  override val id: Short = 10100

  // Always 1 since JDA doesn't seem to support more than that per instance
  private var maxShardCount: Short = 1

  override fun encode() {
    payload.putShort(maxShardCount)
  }

  override fun decode() {
    maxShardCount = payload.short
  }
}
