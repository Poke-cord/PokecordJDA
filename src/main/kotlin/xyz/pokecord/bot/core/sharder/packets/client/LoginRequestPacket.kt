package xyz.pokecord.bot.core.sharder.packets.client

import xyz.pokecord.bot.core.sharder.packets.Packet

class LoginRequestPacket : Packet() {
  override val id: Short = 10101

  var botToken: String? = null
  private var shardIds: ShortArray = shortArrayOf()

  override fun encode() {
    payload.putIString(botToken)
    payload.putShort(shardIds.size.toShort())
    for (shardId in shardIds) {
      payload.putShort(shardId)
    }
  }

  override fun decode() {
    botToken = payload.iString
    val shardCount = payload.short
    shardIds = ShortArray(shardCount.toInt())
    for (i in 1..shardCount) {
      shardIds[i] = payload.short
    }
  }
}
