package xyz.pokecord.bot.utils

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.ceil
import kotlin.math.round

object VoteUtils {
  fun getCurrentSeason(startTimestamp: Long = Config.voteSeasonStartTimestamp): Int {
    val daysSinceStartTimestamp =
      round((System.currentTimeMillis() - startTimestamp) / (1000 * 3600 * 24).toDouble()).toInt()
    var season = 1
    var tmp = 0
    for (i in 0 until daysSinceStartTimestamp) {
      tmp++
      if (tmp == 15) {
        season++
        tmp = 0
      }
    }
    return season
  }

  fun getSeasonEndTime(startTimestamp: Long = Config.voteSeasonStartTimestamp): String {
    val daysSinceStartTimestamp =
      round((System.currentTimeMillis() - startTimestamp) / (1000 * 3600 * 24).toDouble()).toInt()
    val daysLeft = (15 * ceil(daysSinceStartTimestamp / 15.0) - daysSinceStartTimestamp).toInt() + 1
    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
      .format(ZonedDateTime.of(LocalDate.now().plusDays(daysLeft.toLong()).atStartOfDay(), ZoneId.of("UTC")))
  }
}
