package xyz.pokecord.bot.core.sharder

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class DynamicByteBuffer(initialCapacity: Int = 64, private var operationMode: OperationMode = OperationMode.NONE) {
  companion object {
    fun fromByteBuffer(byteBuffer: ByteBuffer, position: Int? = null): DynamicByteBuffer {
      val dynamicByteBuffer = DynamicByteBuffer()
      val byteArray = byteBuffer.toByteArray(position)
      dynamicByteBuffer.byteBuffer = ByteBuffer.wrap(byteArray)
      return dynamicByteBuffer
    }
  }

  enum class OperationMode {
    NONE,
    READ,
    WRITE
  }

  var position = 0

  private var byteBuffer = ByteBuffer.allocate(initialCapacity)

  private fun ensureOperationMode(operationMode: OperationMode) {
    if (this.operationMode == OperationMode.NONE) {
      this.operationMode = operationMode
    } else if (this.operationMode != operationMode) {
      throw IllegalStateException(
        "Current DynamicByteBuffer has already been initialized for ${
          this.operationMode.toString().lowercase()
        } mode."
      )
    }
  }

  private fun ensureCapacity(count: Int) {
    ensureOperationMode(OperationMode.WRITE)
    this.position += count
    val capacity = byteBuffer.capacity()
    val position = byteBuffer.position()
    if (position + count > capacity) {
      val byteArray = byteBuffer.toByteArray()
      byteBuffer = ByteBuffer.wrap(byteArray + ByteArray(64))
      byteBuffer.position(position)
    }
  }

  private val limit: Int
    get() = byteBuffer.limit()

  val size: Int
    get() {
      val position = byteBuffer.position()
      return (if (operationMode == OperationMode.WRITE || position in 1 until limit) position else if (position > 0) limit - position else limit)
    }

  val buffer: ByteBuffer
    get() {
      val offset = if (operationMode == OperationMode.WRITE) 0 else byteBuffer.position()
      val length = if (operationMode == OperationMode.WRITE) byteBuffer.position() else size
      val byteArray = ByteArray(length)
      if (operationMode == OperationMode.WRITE) {
        byteBuffer.flip()
      }
      byteBuffer[byteArray, offset, length]
      val returnByteBuffer = ByteBuffer.wrap(byteArray)
      return returnByteBuffer.position(size)
    }

  private val int: Int
    get() {
      ensureOperationMode(OperationMode.READ)
      position -= 4
      return byteBuffer.int
    }

  val iString: String?
    get() {
      ensureOperationMode(OperationMode.READ)
      val strLength = int
      if (strLength == -1) return null
      val byteArray = ByteArray(strLength)
      byteBuffer.get(byteArray)
      return byteArray.toString(StandardCharsets.UTF_8)
    }

  val short: Short
    get() {
      ensureOperationMode(OperationMode.READ)
      position -= 2
      return byteBuffer.short
    }

  //  fun put(dynamicByteBuffer: DynamicByteBuffer) {
//    put(dynamicByteBuffer.byteBuffer)
//  }
//
//  fun put(byteBuffer: ByteBuffer) {
//    put(byteBuffer.toByteArray())
//  }
//
  private fun put(byteArray: ByteArray) {
    ensureCapacity(byteArray.size)
    byteBuffer.put(byteArray)
  }

  fun putShort(value: Short) {
    ensureCapacity(2)
    byteBuffer.putShort(value)
  }

  fun putInt(value: Int) {
    ensureCapacity(4)
    byteBuffer.putInt(value)
  }

  fun putIString(value: String?) {
    if (value == null) {
      putInt(-1)
      return
    } else putInt(value.length)
    put(value.toByteArray(StandardCharsets.UTF_8))
  }
}

//fun main() {
////  val buffer = DynamicByteBuffer.fromByteBuffer(ByteBuffer.wrap(byteArrayOf(0x27, 0x74, 0x00, 0x00)).position(4))
////  println(buffer.position)
////  println(buffer.size)
////  println(buffer.buffer.toHexString())
////  println(buffer.short)
////  println(buffer.position)
////  println(buffer.size)
////  println(buffer.buffer.toHexString())
////  println(buffer.short)
////  println(buffer.position)
////  println(buffer.size)
////  println(buffer.buffer.toHexString())
////  val buffer = DynamicByteBuffer()
////  for (i in 1..17) {
////    println(buffer.position)
////    println(buffer.size)
////    println(buffer.buffer.toHexString())
////    buffer.putInt(5)
////  }
////  println(buffer.position)
////  println(buffer.size)
////  println(buffer.buffer.toHexString())
//}
