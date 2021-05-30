package xyz.pokecord.bot.core.sharder.packets

import kotlin.reflect.KClass

import xyz.pokecord.bot.core.sharder.packets.client.HandshakeRequestPacket
import xyz.pokecord.bot.core.sharder.packets.client.KeepAliveRequestPacket
import xyz.pokecord.bot.core.sharder.packets.client.LoginRequestPacket

import xyz.pokecord.bot.core.sharder.packets.server.HandshakeOkPacket
import xyz.pokecord.bot.core.sharder.packets.server.KeepAliveOkPacket
import xyz.pokecord.bot.core.sharder.packets.server.LoginOkPacket
import xyz.pokecord.bot.core.sharder.packets.server.ShardInfoPacket

object PacketFactory : HashMap<Short, KClass<out Packet>>() {
  init {
    // client to server packets
    this[10100] = HandshakeRequestPacket::class
    this[10101] = LoginRequestPacket::class
    this[10108] = KeepAliveRequestPacket::class

    // server to client packets
    this[20100] = HandshakeOkPacket::class
    this[20104] = LoginOkPacket::class
    this[20105] = ShardInfoPacket::class
    this[20108] = KeepAliveOkPacket::class
  }
}
