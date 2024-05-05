package xyz.pokecord.bot.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

object Config {
  const val version = "2.2.22"

  val voteSeasonStartDate: LocalDate = Instant.ofEpochMilli(1609459200000).atZone(ZoneOffset.UTC).toLocalDate()

  val officialServerOnlyMode = System.getenv("OFFICIAL_SERVER_ONLY") != null

  val devs =
    mutableListOf(
      "574951722645192734", //Zihad
      "693914342625771551", //Mystic
      "312135171656646658", //Blue
      "610861621287583752", //Zihad
      "730225189756862476", //Mystic
    )

  const val mainServer = "718872125490069534"
  const val testingServer = "757972619986337823"

  const val boostCooldown = 2_592_000_000L
  const val transferChunkSize = 1000
  const val reindexChunkSize = 1000
  const val defaultAuctionTime = 4 * 60 * 60 * 1000L
  const val defaultStartingBid = 1000
  const val defaultBidIncrement = 100

  const val minAuctionTime = 4 * 60 * 60 * 1000L
  const val maxAuctionTime = 30 * 24 * 60 * 60 * 1000L

  const val maxTradeSessionPokemon = 20
  const val maxReleaseSessionPokemon = 50

  val publicNotificationWebhook: String? = System.getenv("PUBLIC_NOTIFICATION_WEBHOOK")
  val donationNotificationWebhook: String? = System.getenv("DONATION_NOTIFICATION_WEBHOOK")

  val officialServers = listOf(
    mainServer,
    testingServer
  )

  object Emojis {
    val alphabet = listOf(
      "🇦",
      "🇧",
      "🇨",
      "🇩",
      "🇪",
      "🇫",
      "🇬",
      "🇭",
      "🇮",
      "🇯",
      "🇰",
      "🇱",
      "🇲",
      "🇳",
      "🇴",
      "🇵",
      "🇶",
      "🇷",
      "🇸",
      "🇹",
      "🇺",
      "🇻",
      "🇼",
      "🇽",
      "🇾",
      "🇿"
    )

    const val EVENT_SHINY = "💠"
    const val EVENT = "🔷"
    const val SHINY = "⭐"
  }

  enum class Roles(val id: String) {
    OWNER("719524189815439371"),
    STAFF("719524196417536150");

    companion object {
      val ids = values().map { it.id }
    }
  }

  object StatVoiceChannels {
    const val guilds = "720611061140684871"
    const val users = "720611164752445531"
    const val monthlyVotes = "731057601877377054"
  }
}
