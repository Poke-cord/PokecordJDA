package xyz.pokecord.bot.core.sharder.packets

import xyz.pokecord.bot.core.sharder.DynamicByteBuffer
import xyz.pokecord.bot.core.sharder.Session

abstract class Packet {
  abstract val id: Short

  lateinit var payload: DynamicByteBuffer
  lateinit var session: Session

  open fun decode() {}
  open fun encode() {}

  open suspend fun processSend() {}
  open suspend fun processReceive() {}
}
