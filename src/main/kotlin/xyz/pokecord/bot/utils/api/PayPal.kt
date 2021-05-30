package xyz.pokecord.bot.utils.api

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.PaypalCredentials
import xyz.pokecord.bot.utils.Json
import java.util.*

class PayPal(val database: Database) {
  private enum class Domain(val domain: String) {
    Regular("www.paypal.com"),
    Sandbox("sandbox.paypal.com")
  }

  private enum class Endpoint(val path: String) {
    Order("/v2/checkout/orders"),
    Token("/v1/oauth2/token")
  }

  private enum class Redirect(val path: String) {
    Complete("/order/confirm"),
    Cancel("/order/cancel")
  }

  @Serializable
  private data class TokenResponse(
    val access_token: String,
    val expires_in: Int
  )

  private val clientId = System.getenv("PAYPAL_CLIENT_ID")
  private val clientSecret = System.getenv("PAYPAL_CLIENT_SECRET")

  private val devEnv = System.getenv("DEV").let { it != null && it != "false" }

  val apiRoot = if (devEnv) "https://api-m.sandbox.paypal.com" else "https://api-m.paypal.com"
  val root = if (devEnv) "http://localhost:3000" else "https://pokecord.xyz"

  private val base64Encoder by lazy { Base64.getEncoder() }
  private val ktorClient by lazy {
    HttpClient {
      install(JsonFeature) {
        serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
          ignoreUnknownKeys = true
        })
      }
    }
  }

  private suspend fun getAccessToken(): String {
    val paypalCredentials = database.configRepository.getPaypalCredentials()
    if (paypalCredentials?.accessToken != null && paypalCredentials.endsAt.time > System.currentTimeMillis() + 300e3) {
      return paypalCredentials.accessToken
    }

    val auth = base64Encoder.encodeToString("${clientId}:${clientSecret}".toByteArray())
    val tokenResponse = ktorClient.post<TokenResponse>("${apiRoot}${Endpoint.Token.path}") {
      body = "grant_type=client_credentials"

      header("Accept", "application/json")
      header("Accept-Language", "en-US")
      header("Authorization", "Basic $auth")
      header("Content-Type", "application/x-www-form-urlencoded")
    }
    database.configRepository.setPaypalCredentials(
      PaypalCredentials(
        tokenResponse.access_token,
        Date(System.currentTimeMillis() + (tokenResponse.expires_in * 1000))
      )
    )
    return tokenResponse.access_token
  }

//  suspend fun captureOrder(orderId: String): Boolean {
//    val accessToken = getAccessToken()
//    val httpResponse = ktorClient.post<HttpResponse>("${apiRoot}${Endpoint.Order.path}/${orderId}/capture") {
//      header("Authorization", "Bearer $accessToken")
//    }
//    return httpResponse.status == HttpStatusCode.OK
//  }

  suspend fun createOrder(
    username: String,
    price: Number,
    product: String
  ): String? {
    val accessToken = getAccessToken()
    val httpResponse = ktorClient.post<HttpResponse>("${apiRoot}${Endpoint.Order.path}") {
      body = buildJsonObject {
        put("intent", "CAPTURE")
        putJsonObject("application_context") {
          put("return_url", "${root}${Redirect.Complete.path}")
          put("cancel_url", "${root}${Redirect.Cancel.path}")
          put("brand_name", "Pokecord")
          put("locale", "en-US")
          put("landing_page", "BILLING")
          put("user_action", "CONTINUE")
        }
        putJsonArray("purchase_units") {
          addJsonObject {
            putJsonObject("amount") {
              put("currency_code", "USD")
              put("value", price.toString())
            }
            put("description", "$username is purchasing \"${product}\" and agreeing to the Pokecord TOS.")
          }
        }
      }

      header("Authorization", "Bearer $accessToken")
      header("Content-Type", "application/json")
    }

    if (httpResponse.status == HttpStatusCode.Created) {
      val jsonText = httpResponse.readText()
      val json = Json.parseToJsonElement(jsonText)
      if (json is JsonObject) {
        val id = json["id"]
        if (id is JsonPrimitive) {
          return id.content
        }
      }
    }
    return null
  }

  suspend fun deleteOrder(orderId: String): Boolean {
    val accessToken = getAccessToken()
    val httpResponse = ktorClient.delete<HttpResponse>("${apiRoot}${Endpoint.Order.path}/${orderId}") {
      header("Authorization", "Bearer $accessToken")
    }
    return httpResponse.status == HttpStatusCode.OK
  }

  fun getCheckoutLink(orderId: String): String {
    val domain = if (devEnv) Domain.Sandbox.domain else Domain.Regular.domain
    return "https://${domain}/checkoutnow?token=${orderId}"
  }
}
