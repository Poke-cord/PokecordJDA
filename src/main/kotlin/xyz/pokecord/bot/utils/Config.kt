package xyz.pokecord.bot.utils

object Config {
  const val version = "2.0.0"
  const val voteSeasonStartTimestamp = 1609459200000

  val officialServerOnlyMode = System.getenv("OFFICIAL_SERVER_ONLY") != null

  val devs =
    mutableListOf("584915458302672916", "574951722645192734", "693914342625771551", "610861621287583752")

  const val mainServer = "718872125490069534"
  const val testingServer = "757972619986337823"

  const val publicNotificationWebhook =
    "https://discord.com/api/webhooks/728471507667255297/BBjKGrtaC6pCBcJfVc-eA79SIjx72T-vtAOguSjkMJCcefSi2q_I4ejMrBZD0agBAGet"
  const val donationNotificationWebhook =
    "https://discord.com/api/webhooks/743592059231010976/OWTZ4wUvZXE8DL5YqFOo8WS4uRolpf6eSnDBL-o8b2OQA3Rfj1XmGk6rI4TU5i-8hEXd"

  val officialServers = listOf(
    mainServer,
    testingServer
  )

  enum class VoteRewards(
    val minTier: Int,
    val maxTier: Int,
    val minCredits: Int,
    val maxCredits: Int,
    val minXp: Int,
    val maxXp: Int,
    val minGems: Int,
    val maxGems: Int
  ) {
    TIER1(0, 10, 500, 2500, 500, 2500, 5, 7),
    TIER2(11, 20, 500, 2500, 500, 2500, 7, 9),
    TIER3(21, 30, 500, 2500, 500, 2500, 10, 12)
  }

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
}
