package xyz.pokecord.bot.utils

import java.time.LocalDateTime

object DayNightUtils {
  fun getCurrentTime(local: LocalDateTime = LocalDateTime.now()): String {
    val hour = local.hour
    return if (hour % 2 == 0) "day" else "night"
  }
}
