package xyz.pokecord.bot.core.structures.discord

import kotlinx.coroutines.sync.Mutex

object SpawnChannelMutex {
  private val mutexList = mutableMapOf<String, Mutex>()

  operator fun get(channelId: String): Mutex {
    return mutexList.getOrElse(channelId) {
      Mutex().also {
        mutexList[channelId] = it
      }
    }
  }
}
