package xyz.pokecord.bot.utils.api

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import xyz.pokecord.bot.utils.Config

class TopGG(
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
      header("Authorization", token)
      userAgent("DiscordBot (https://pokecord.xyz, ${Config.version})")
    }
  }

  suspend fun postServerCount(botId: String, serverCounts: List<Int>) {
    httpClient.post<HttpResponse>(  "${baseUrl}/bots/${botId}/stats") {
      body = buildJsonObject {
        putJsonArray("shards") {
          serverCounts.forEach {
            add(it)
          }
        }
      }
      contentType(ContentType.Application.Json)
    }
  }

  companion object {
    private const val baseUrl = "https://top.gg/api"
  }
}
