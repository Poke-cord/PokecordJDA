package xyz.pokecord.bot.utils

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object VoteUtils {
  fun getCurrentSeason(startDate: LocalDate = Config.voteSeasonStartDate, now: LocalDate = LocalDate.now()): Int {
    val daysSinceSeasonStarted = startDate.until(now, ChronoUnit.DAYS)
    return (daysSinceSeasonStarted / 30).toInt() + 1
  }

  fun getSeasonDay(startDate: LocalDate = Config.voteSeasonStartDate, now: LocalDate = LocalDate.now()): Int {
    val daysSinceSeasonStarted = startDate.until(now, ChronoUnit.DAYS)
    return (daysSinceSeasonStarted % 30).toInt() + 1
  }

  fun getSeasonEndTime(
    startDate: LocalDate = Config.voteSeasonStartDate,
    now: LocalDate = LocalDate.now()
  ): LocalDate {
    val daysUntilNextSeason = (getCurrentSeason(startDate, now)) * 30
    return startDate.plusDays(daysUntilNextSeason.toLong())
  }
}
