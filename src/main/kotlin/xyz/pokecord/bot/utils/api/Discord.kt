package xyz.pokecord.bot.utils.api

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import xyz.pokecord.bot.utils.Config

class Discord(
  val token: String
) {
  private val httpClient = HttpClient()

  suspend fun giveMemberRole(guildId: String, userId: String, roleId: String): Boolean {
    val response = httpClient.put<HttpResponse>("${baseUrl}/guilds/${guildId}/members/${userId}/roles/${roleId}") {
      header("Authorization", token)
      userAgent("DiscordBot (https://pokecord.xyz, ${Config.version})")
    }
    return response.status == HttpStatusCode.OK
  }

  companion object {
    private const val baseUrl = "https://discord.com/api/v9"
  }
}
