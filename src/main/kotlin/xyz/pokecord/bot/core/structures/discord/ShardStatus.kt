package xyz.pokecord.bot.core.structures.discord

import kotlinx.serialization.Serializable

@Serializable
data class ShardStatus(
  val id: Int,
  val count: Int,
  val hostname: String,
  val gatewayPing: Long,
  val guildCacheSize: Long,
  val updatedAt: Long,
)
