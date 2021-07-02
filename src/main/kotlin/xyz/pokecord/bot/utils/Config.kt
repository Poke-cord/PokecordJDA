package xyz.pokecord.bot.utils

object Config {
  const val version = "2.0.0"
  const val voteSeasonStartTimestamp = 1609459200000

  val officialServerOnlyMode = System.getenv("OFFICIAL_SERVER_ONLY") != null

  val devs =
    mutableListOf("584915458302672916", "574951722645192734", "693914342625771551", "610861621287583752")

  const val mainServer = "718872125490069534"
  const val testingServer = "757972619986337823"

  const val publicNotificationChannel = "719524224036765737"

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
