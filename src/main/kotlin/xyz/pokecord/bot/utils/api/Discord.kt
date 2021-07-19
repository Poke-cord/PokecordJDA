package xyz.pokecord.bot.utils.api

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import xyz.pokecord.bot.utils.Config
import xyz.pokecord.bot.utils.GuildMemberResponse
import xyz.pokecord.bot.utils.GuildRoleResponse

class Discord(
  val token: String
) {
  private val httpClient = HttpClient {
    install(JsonFeature) {
      serializer = KotlinxSerializer(
        kotlinx.serialization.json.Json {
          ignoreUnknownKeys = true
        }
      )
    }
    defaultRequest {
      header("Authorization", if (token.startsWith("Bot ")) token else "Bot $token")
      userAgent("DiscordBot (https://pokecord.xyz, ${Config.version})")
    }
  }

  suspend fun giveMemberRole(guildId: String, userId: String, roleId: String): Boolean {
    val response = httpClient.put<HttpResponse>("${baseUrl}/guilds/${guildId}/members/${userId}/roles/${roleId}")
    return response.status == HttpStatusCode.OK
  }

  suspend fun getGuildRoles(guildId: String): List<GuildRoleResponse> {
    return httpClient.get("${baseUrl}/guilds/${guildId}/roles")
  }

  suspend fun getGuildMember(guildId: String, userId: String): GuildMemberResponse {
    return httpClient.get("${baseUrl}/guilds/${guildId}/members/${userId}")
  }

  companion object {
    private const val baseUrl = "https://discord.com/api/v9"
  }
}
