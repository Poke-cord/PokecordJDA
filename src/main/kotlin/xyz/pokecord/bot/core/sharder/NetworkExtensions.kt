package xyz.pokecord.bot.core.sharder

import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.experimental.and

class ReadWriteAttachment(val buffer: ByteBuffer, val continuation: Continuation<Int>)

suspend fun AsynchronousSocketChannel.asyncRead(buffer: ByteBuffer): Int {
  return suspendCoroutine { continuation ->
    this.read(buffer, ReadWriteAttachment(buffer, continuation), ReadCompletionHandler)
  }
}

suspend fun AsynchronousSocketChannel.asyncWrite(buffer: ByteBuffer): Int {
  return suspendCoroutine { continuation ->
    buffer.flip()
    this.write(buffer, ReadWriteAttachment(buffer, continuation), WriteCompletionHandler)
  }
}

suspend fun AsynchronousSocketChannel.asyncConnect(remote: SocketAddress) {
  return suspendCoroutine { continuation ->
    this.connect(remote, continuation, ConnectCompletionHandler)
  }
}

object ConnectCompletionHandler : CompletionHandler<Void, Continuation<Unit>> {
  override fun completed(result: Void?, attachment: Continuation<Unit>) {
    attachment.resume(Unit)
  }

  override fun failed(exc: Throwable, attachment: Continuation<Unit>) {
    attachment.resumeWithException(exc)
  }
}

object ReadCompletionHandler : CompletionHandler<Int, ReadWriteAttachment> {
  override fun completed(result: Int, attachment: ReadWriteAttachment) {
    attachment.buffer.flip()
    attachment.continuation.resume(result)
  }

  override fun failed(exc: Throwable, attachment: ReadWriteAttachment) {
    attachment.continuation.resumeWithException(exc)
  }
}

object WriteCompletionHandler : CompletionHandler<Int, ReadWriteAttachment> {
  override fun completed(result: Int, attachment: ReadWriteAttachment) {
    attachment.continuation.resume(result)
  }

  override fun failed(exc: Throwable, attachment: ReadWriteAttachment) {
    attachment.continuation.resumeWithException(exc)
  }
}

fun Byte.toHex(): String {
  val hexDigits = CharArray(2)
  hexDigits[0] = Character.forDigit(this.toInt() shr 4 and 0xF, 16)
  hexDigits[1] = Character.forDigit((this and 0xF).toInt(), 16)
  return String(hexDigits)
}

fun ByteArray.toHexString(): String {
  val hexStringBuffer = StringBuffer()
  for (i in this.indices) {
    hexStringBuffer.append(this[i].toHex())
  }
  return hexStringBuffer.toString()
}

fun ByteBuffer.toHexString(position: Int? = null): String {
  return toByteArray(position).toHexString()
}

fun ByteBuffer.toByteArray(position: Int? = null): ByteArray {
  return array().slice(IntRange(0, if (position != null) position - 1 else position() - 1)).toByteArray()
}
