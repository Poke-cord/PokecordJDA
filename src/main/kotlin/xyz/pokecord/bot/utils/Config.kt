package xyz.pokecord.bot.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

object Config {
  const val version = "2.0.0"

  val voteSeasonStartDate: LocalDate = Instant.ofEpochMilli(1609459200000).atZone(ZoneOffset.UTC).toLocalDate()

  val officialServerOnlyMode = System.getenv("OFFICIAL_SERVER_ONLY") != null

  val devs =
    mutableListOf("584915458302672916", "574951722645192734", "693914342625771551", "610861621287583752", "341282855625818122")

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

  const val publicNotificationWebhook =
    "https://discord.com/api/webhooks/728471507667255297/BBjKGrtaC6pCBcJfVc-eA79SIjx72T-vtAOguSjkMJCcefSi2q_I4ejMrBZD0agBAGet"
  const val donationNotificationWebhook =
    "https://discord.com/api/webhooks/743592059231010976/OWTZ4wUvZXE8DL5YqFOo8WS4uRolpf6eSnDBL-o8b2OQA3Rfj1XmGk6rI4TU5i-8hEXd"

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
