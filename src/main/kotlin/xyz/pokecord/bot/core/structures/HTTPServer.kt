package xyz.pokecord.bot.core.structures

import club.minnced.discord.webhook.WebhookClientBuilder
import dev.minn.jda.ktx.EmbedBuilder
import dev.minn.jda.ktx.await
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.JDA
import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.core.managers.I18n
import xyz.pokecord.bot.core.managers.database.models.Order
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.ShardStatus
import xyz.pokecord.bot.core.structures.pokemon.items.CCTItem
import xyz.pokecord.bot.core.structures.store.packages.Package
import xyz.pokecord.bot.utils.CachedStaffMember
import xyz.pokecord.bot.utils.Config
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.VoteUtils
import xyz.pokecord.bot.utils.api.PayPal
import xyz.pokecord.bot.utils.extensions.awaitSuspending

class HTTPServer(val bot: Bot) {
  @Serializable
  data class BotVoteArgs(
    val bot: String,
    val user: String,
    val type: String,
    val query: String?
  )

  @Serializable
  data class OrderSuccessResponse(
    val paid: Boolean,
    val name: String,
    val price: Double,
    val username: String,
  )

  private val topggSecret = System.getenv("TOPGG_SECRET") ?: throw Exception("top.gg secret is required.")

  private val publicNotificationWebhookClient = WebhookClientBuilder(Config.publicNotificationWebhook).buildJDA()
  private val donationNotificationWebhookClient = WebhookClientBuilder(Config.donationNotificationWebhook).buildJDA()

  private suspend fun sendVoteNotification(userId: String) {
    publicNotificationWebhookClient.send(
      EmbedBuilder {
        color = 0xf0e365
        description =
          "Big thanks to <@${userId}> for voting over at [top.gg](https://top.gg/bot/705016654341472327/vote)!"
        title = "Thank You for Voting!"

        footer("Support us by using the p!donate command.")
      }.build()
    ).await()
  }

  suspend fun sendBoostNotification(userId: String, redeemName: String) {
    publicNotificationWebhookClient.send(
      EmbedBuilder {
        color = 0xf0e365
        description =
          "Thank you to <@${userId}> for boosting, they were rewarded with a **$redeemName**!"
        title = "Thank You for Boosting!"

        footer("Support us by using the p!donate command.")
      }.build()
    ).await()
  }

  private suspend fun sendDonationNotification(order: Order, orderInfo: PayPal.OrderInfo, itemName: String) {
    publicNotificationWebhookClient.send(
      EmbedBuilder {
        color = 0xf0e365
        description =
          "Big thanks to <@${order.userId}> for donating!"
        title = "Thank You for Donating!"

        footer("Support us by using the p!donate command.")
      }.build()
    ).await()

    donationNotificationWebhookClient.send(
      EmbedBuilder {
        color = 0xf0e365
        description = """            
        **User**: <@${order.userId}> [${order.userId}]
        **Amount**: ${order.price}
        **Status**: ${orderInfo.status ?: "N/A"}
        **Package**: $itemName
        """.trimIndent()
        title = "New Purchase"
      }.build()
    )
  }

  private suspend fun onVote(args: BotVoteArgs) {
    // days 5, 10, 15, 20, 25, 30 (aka when day % 5 == 0) -> 10k credits, 1 CCT, 1 Token
    // else -> 5k credits, 1 Token
    val day = VoteUtils.getSeasonDay()

    val (credits, tokens, cct) =
      if (day % 5 == 0) Triple(10_000, 1, 1)
      else Triple(5_000, 1, 0)

    val session = bot.database.startSession()
    session.use { clientSession ->
      clientSession.startTransaction()
      val userData = bot.database.userRepository.getUser(args.user)
      bot.database.userRepository.setLastVoteTime(userData, session = clientSession)
      bot.database.userRepository.incCredits(userData, credits, clientSession)
      bot.database.userRepository.incTokens(userData, tokens, clientSession)
      if (cct > 0) {
        bot.database.userRepository.addInventoryItem(args.user, CCTItem.id, cct, clientSession)
      }
      clientSession.commitTransactionAndAwait()
    }
    sendVoteNotification(args.user)
  }

  suspend fun ApplicationCall.respond(statusCode: HttpStatusCode) {
    respondText(statusCode.toString(), status = statusCode)
  }

  fun start() {
    val port = System.getenv("HTTP_PORT")?.toIntOrNull() ?: 9999
    embeddedServer(Jetty, port = port) {
      install(ContentNegotiation) {
        json(Json {
          ignoreUnknownKeys = true
        })
      }

      routing {
        get("/api/misc/team") {
          val staffMemberObjects = bot.cache.staffMembersSet.readAllAsync().awaitSuspending().map {
            Json.decodeFromString<CachedStaffMember>(it)
          }
          call.respond(staffMemberObjects)
        }
        get("/api/stats/pokemonCount") {
          val pokemonCount = bot.database.pokemonRepository.getEstimatedPokemonCount()
          call.respond(
            buildJsonObject {
              put("count", pokemonCount)
            }
          )
        }
        get("/api/stats/userCount") {
          val userCount = bot.database.userRepository.getEstimatedUserCount()
          call.respond(
            buildJsonObject {
              put("count", userCount)
            }
          )
        }
        get("/api/stats/serverCount") {
          val serverCount = bot.cache.shardStatusMap.readAllValuesAsync().awaitSuspending()
            .map { json -> Json.decodeFromString<ShardStatus>(json) }
            .sumOf { it.guildCacheSize }
          call.respond(
            buildJsonObject {
              put("count", serverCount)
            }
          )
        }

        post("/topgg/vote") {
          val secret = call.request.header("Authorization")
          if (secret != topggSecret) {
            call.respond(HttpStatusCode.Unauthorized)
            return@post
          }
          if (bot.shardManager.shardsRunning < 1) {
            call.respond(HttpStatusCode.InternalServerError)
            return@post
          }
          val voteArgs = call.receive<BotVoteArgs>()
          if (voteArgs.bot == bot.shardManager.shards.first().selfUser.id && (voteArgs.type == "upvote" || (voteArgs.type == "test" && bot.devEnv))) {
            onVote(voteArgs)
            call.respond(HttpStatusCode.OK)
          } else {
            call.respond(HttpStatusCode.BadRequest)
          }
        }

        get("/api/orders/id/{orderId?}") {
          val orderId = call.parameters["orderId"]
          if (orderId == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
          }

          val order = bot.database.orderRepository.getOrder(orderId)
          if (order == null) {
            call.respondText(
              "The order ID provided is invalid and could not be found in the database.",
              status = HttpStatusCode.NotFound
            )
          } else {
            var itemName = "Unknown"
            val userData = bot.database.userRepository.getUser(order.userId, order.userName)
            val `package` = Package.packages.find {
              it.id == order.packageId
            }
            `package`?.let {
              val item = `package`.items.find {
                it.id == order.itemId
              }
              item?.let {
                itemName = I18n.translate(null, "store.packages.${`package`.id}.items.${item.id}")
                `package`.giveReward(bot, userData, item)
              }
            }

            call.respond(
              HttpStatusCode.OK,
              OrderSuccessResponse(
                order.paid,
                itemName,
                order.price,
                order.userName
              )
            )
          }
        }
        get("/api/orders/cancel/{orderId?}") {
          val orderId = call.parameters["orderId"]
          if (orderId == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
          }

          val order = bot.database.orderRepository.getOrder(orderId)
          if (order == null) {
            call.respondText(
              "The order ID provided is invalid and could not be found in the database.",
              status = HttpStatusCode.NotFound
            )
          } else {
            val deleted = bot.payPal.deleteOrder(orderId)
            if (deleted) {
              bot.database.orderRepository.deleteOrder(order)
              call.respondText("Order cancelled", status = HttpStatusCode.OK)
            } else {
              call.respondText("Failed to cancel order", status = HttpStatusCode.InternalServerError)
            }
          }
        }
        get("/api/orders/confirm/{orderId?}") {
          val orderId = call.parameters["orderId"]
          if (orderId == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
          }

          val order = bot.database.orderRepository.getOrder(orderId)
          if (order == null) {
            call.respondText(
              "The order ID provided is invalid and could not be found in the database.",
              status = HttpStatusCode.NotFound
            )
          } else {
            try {
              var orderInfo = bot.payPal.getOrderInfo(orderId)
              if (orderInfo.status == PayPal.OrderStatus.APPROVED) {
                orderInfo = bot.payPal.captureOrder(order.orderId)
              }
              if (orderInfo.status != PayPal.OrderStatus.COMPLETED) {
                call.respondText(
                  "The order with id ${order.orderId} is not paid yet, try again later. Status: ${orderInfo.status}",
                  status = HttpStatusCode.BadRequest
                )
              } else {
                if (!order.paid) {
                  var itemName = "Unknown"
                  val userData = bot.database.userRepository.getUser(order.userId, order.userName)
                  val `package` = Package.packages.find {
                    it.id == order.packageId
                  }
                  `package`?.let {
                    val item = `package`.items.find {
                      it.id == order.itemId
                    }
                    item?.let {
                      itemName = I18n.translate(null, "store.packages.${`package`.id}.items.${item.id}")
                      `package`.giveReward(bot, userData, item)
                    }
                  }

                  val payerEmail = orderInfo.purchaseUnits?.firstOrNull()?.payee?.emailAddress
                    ?: orderInfo.payer?.emailAddress
                  if (payerEmail != null && order.payeeEmail != payerEmail) {
                    order.payeeEmail = payerEmail
                  }
                  order.paid = true
                  bot.database.orderRepository.replaceOrder(order)

                  call.respond(
                    HttpStatusCode.OK,
                    OrderSuccessResponse(
                      order.paid,
                      itemName,
                      order.price,
                      order.userName
                    )
                  )

                  sendDonationNotification(order, orderInfo, itemName)
                } else {
                  call.respondText(
                    "This order has already been paid.",
                    status = HttpStatusCode.Accepted
                  )
                }
              }
            } catch (e: Throwable) {
              e.printStackTrace()
            }
          }
        }

        get("/_/internal/ready") {
          try {
            if (bot.shardManager.statuses.all { it.value == JDA.Status.CONNECTED }) {
              return@get call.respond(HttpStatusCode.OK)
            }
          } catch (e: UninitializedPropertyAccessException) {
          }
          call.respond(HttpStatusCode.ServiceUnavailable)
        }
      }
    }.start()
  }
}
