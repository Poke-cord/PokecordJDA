package xyz.pokecord.bot.core.sharder.packets.client

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.pokecord.bot.core.sharder.packets.Packet

class ShardReadyPacket : Packet() {
  override val id: Short = 10102

  // Always 1 since JDA doesn't seem to support more than that per instance
  private var shardIds = listOf(1)

  override fun encode() {
    payload.putShort(shardIds.size.toShort())
    shardIds.forEach {
      payload.putShort(it.toShort())
    }
  }

  override fun decode() {
    val shardCount = payload.short
    val shardIds = mutableListOf<Int>()
    for (i in 0..shardCount) {
      shardIds.add(payload.short.toInt())
    }
  }

  override suspend fun processSend() {
    session.client.logger.info("Shard reported as ready! Starting sending keep alive requests...")
    val keepAliveRequestPacket = KeepAliveRequestPacket()
    session.keepAliveSenderJob = session.client.coroutineScope.launch {
      while (true) {
        launch {
          session.sendPacket(keepAliveRequestPacket)
          session.client.logger.debug("Keep alive request sent!")
        }
        delay(10000)
      }
    }
  }
}
