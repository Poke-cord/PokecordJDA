package xyz.pokecord.bot.core.sharder

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.pokecord.bot.core.sharder.packets.client.HandshakeRequestPacket
import xyz.pokecord.bot.core.sharder.packets.client.ShardReadyPacket
import java.io.IOException
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.Executors
import kotlin.system.exitProcess

@Suppress("UNUSED")
class SharderClient(
  val address: SocketAddress,
  private val socket: AsynchronousSocketChannel = AsynchronousSocketChannel.open()
) {
  constructor(
    hostname: String = "localhost",
    port: Int = 5252,
    socket: AsynchronousSocketChannel = AsynchronousSocketChannel.open()
  ) : this(InetSocketAddress(hostname, port), socket)

  private data class PacketHolder(val header: ByteBuffer, val payload: ByteBuffer)

  val logger: Logger = LoggerFactory.getLogger(SharderClient::class.java)
  val session: Session = Session(this)

  private var receiving = false
  private var currentChannel: Channel<PacketHolder>? = null

  val coroutineScope = CoroutineScope(Executors.newCachedThreadPool().asCoroutineDispatcher())

  suspend fun connect() {
    socket.asyncConnect(address)
  }

  suspend fun send(buffer: ByteBuffer): Int {
    return socket.asyncWrite(buffer)
  }

  suspend fun startReceiving() {
    currentChannel = Channel()
    coroutineScope.launch(Dispatchers.IO) {
      receiving = true
      while (socket.isOpen && receiving) {
        try {
          val headerPayload = ByteBuffer.allocate(6)
          val readBytes = socket.asyncRead(headerPayload)
          if (readBytes == 6) {
            headerPayload.position(readBytes)
            val length = headerPayload.getInt(2)
            var packet = ByteBuffer.allocate(0)
            var totalRead = 0
            while (totalRead < length) {
              val buffer = ByteBuffer.allocate(length - totalRead)
              val read = socket.asyncRead(buffer)
              totalRead += read
              buffer.position(read)
              packet = ByteBuffer.wrap(packet.toByteArray() + buffer.toByteArray()).position(totalRead)
              packet.position(totalRead)
            }
            if (currentChannel != null) currentChannel!!.send(PacketHolder(headerPayload, packet))
          }
        } catch (e: Exception) {
          logger.error("Error while receiving data", e)
//          Sentry.captureException(e)
          if (e is IOException && e.message != null && (e.message!!.contains("The specified network name is no longer available") || e.message!!.contains(
              "An existing connection was forcibly closed by the remote host",
            ))
          ) exitProcess(1)
        }
      }
      receiving = false
      currentChannel?.close()
    }
    coroutineScope.launch {
      while (currentChannel != null) {
        try {
          val packetHolder = currentChannel!!.receive()
          launch {
            session.handlePayload(
              DynamicByteBuffer.fromByteBuffer(packetHolder.header),
              DynamicByteBuffer.fromByteBuffer(packetHolder.payload)
            )
          }
        } catch (e: Exception) {
//          Sentry.captureException(e)
          logger.error("Error trying to receive", e)
        }
      }
    }
  }

  fun stopReceiving() {
    if (receiving) receiving = false
    currentChannel?.close()
    currentChannel = null
  }

  suspend fun reportAsReady() {
    val shardReadyPacket = ShardReadyPacket()
    session.sendPacket(shardReadyPacket)
  }

  suspend fun login() {
    val handshakeRequestPacket = HandshakeRequestPacket()
    session.sendPacket(handshakeRequestPacket)
  }
}
