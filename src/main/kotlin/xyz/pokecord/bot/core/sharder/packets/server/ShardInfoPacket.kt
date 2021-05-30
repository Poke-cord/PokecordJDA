package xyz.pokecord.bot.core.sharder.packets.server

import xyz.pokecord.bot.core.sharder.packets.Packet

class ShardInfoPacket : Packet() {
  override val id: Short = 20105

  var shardCount: Short = 0
  var shardIds: ShortArray = shortArrayOf()

  override fun decode() {
    shardCount = payload.short
    val shardIdCount = payload.short.toInt()
    shardIds = ShortArray(shardIdCount)
    for (i in 0 until shardIdCount) {
      shardIds[i] = payload.short
    }
  }

  override suspend fun processReceive() {
    session.shardCount = shardCount
    session.shardIds = shardIds
    session.client.logger.info("Shard info received: $shardCount,${shardIds.joinToString(", ")}")
  }
}
