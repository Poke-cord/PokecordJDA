package xyz.pokecord.bot.core.sharder

import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import xyz.pokecord.bot.core.sharder.packets.Packet
import xyz.pokecord.bot.core.sharder.packets.PacketFactory
import java.nio.ByteBuffer
import kotlin.reflect.full.createInstance

class Session(val client: SharderClient) {
  var shardCount: Short = 0
  var shardIds: ShortArray = shortArrayOf()

  var lastHeartbeatAt: Long = System.currentTimeMillis() + 30_000
  var keepAliveSenderJob: Job? = null

  val receivedPacketChannel = Channel<Packet>()
  private val sentPacketChannel = Channel<Packet>()

  suspend fun sendPacket(packet: Packet) {
    packet.payload = DynamicByteBuffer()
    packet.session = this
    packet.encode()
    val headerPayload = DynamicByteBuffer(6)
    headerPayload.putShort(packet.id)
    headerPayload.putInt(packet.payload.position)
    client.send(
      ByteBuffer.wrap(
        headerPayload.buffer.toByteArray() + packet.payload.buffer.toByteArray()
      ).position(
        headerPayload.position + packet.payload.position
      )
    )
    packet.processSend()
    sentPacketChannel.send(packet)
  }

  suspend fun handlePayload(header: DynamicByteBuffer, payload: DynamicByteBuffer) {
    if (payload.size < 6) return
    val packetId = header.short
    val packetClass = PacketFactory[packetId]
    if (packetClass != null) {
      val packet = packetClass.createInstance()
      packet.payload = payload
      packet.session = this
      packet.decode()
      packet.processReceive()
      receivedPacketChannel.send(packet)
    } else {
      client.logger.warn("Unknown Packet $packetId received!")
    }
  }
}
