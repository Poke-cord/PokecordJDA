package xyz.pokecord.bot.core.structures

import club.minnced.discord.webhook.WebhookClientBuilder
import dev.minn.jda.ktx.EmbedBuilder
import dev.minn.jda.ktx.await
import io.ktor.application.*
import io.ktor.auth.*
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
import org.litote.kmongo.coroutine.abortTransactionAndAwait
import org.litote.kmongo.coroutine.commitTransactionAndAwait
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule
import xyz.pokecord.bot.core.managers.I18n
import xyz.pokecord.bot.core.managers.database.models.DonateBotTransaction
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.EmbedTemplates
import xyz.pokecord.bot.core.structures.discord.ShardStatus
import xyz.pokecord.bot.core.structures.pokemon.items.CCTItem
import xyz.pokecord.bot.core.structures.store.packages.Package
import xyz.pokecord.bot.utils.CachedStaffMember
import xyz.pokecord.bot.utils.Config
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.VoteUtils
import xyz.pokecord.bot.utils.api.PayPal
import xyz.pokecord.bot.utils.extensions.awaitSuspending
import xyz.pokecord.bot.utils.ReminderUtils

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
  private val donateBotSecret = System.getenv("DONATEBOT_SECRET") ?: throw Exception("donatebot secret is required.")

  private val publicNotificationWebhookClient =
    Config.publicNotificationWebhook?.let { WebhookClientBuilder(it).buildJDA() }
  private val donationNotificationWebhookClient =
    Config.donationNotificationWebhook?.let { WebhookClientBuilder(it).buildJDA() }

  private suspend fun sendVoteNotification(userId: String) {
    publicNotificationWebhookClient?.send(
      EmbedBuilder {
        color = EmbedTemplates.Color.BLUE.code
        description =
          "<@${userId}> voted for [**Pokecord on top.gg**](https://top.gg/bot/705016654341472327/vote)!"
        title = "Thanks for the vote!"

        footer("Check in for more rewards in 12 hours.")
      }.build()
    )?.await()
  }

  suspend fun sendBoostNotification(userId: String, redeemName: String) {
    publicNotificationWebhookClient?.send(
      EmbedBuilder {
        color = EmbedTemplates.Color.BLUE.code
        description =
          "<@${userId}> boosted the server and were awarded a **$redeemName**!"
        title = "Thanks for the boost!"

        footer("Renew your boost in 30 days for another redeem.")
      }.build()
    )?.await()
  }

  private suspend fun sendDonationNotification(
    userId: String,
    price: String,
    itemName: String,
    status: String?,
    privateOnly: Boolean = false,
  ) {
    if (!privateOnly) {
      publicNotificationWebhookClient?.send(
        EmbedBuilder {
          color = EmbedTemplates.Color.BLUE.code
          description = "<@${userId}> made a donation to Pokecord!"
          title = "Thanks for the donation!"

          footer("Rewards have been automatically applied.")
        }.build()
      )?.await()
    }

    donationNotificationWebhookClient?.send(
      EmbedBuilder {
        color = EmbedTemplates.Color.BLUE.code
        description = """            
          **User**: <@${userId}> [${userId}]
          **Amount**: $price
          **Status**: ${status ?: "N/A"}
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

    val userData = bot.database.userRepository.getUser(args.user)

    val session = bot.database.startSession()
    session.use { clientSession ->
      clientSession.startTransaction()
      bot.database.userRepository.setLastVoteTime(userData, session = clientSession)
      if (!bot.database.userRepository.incCredits(userData, credits, clientSession)) {
        clientSession.abortTransactionAndAwait()
        bot.logger.warn("Negative credits encountered while processing vote of user ${args.user}")
        return
      }
      bot.database.userRepository.incTokens(userData, tokens, clientSession)
      if (cct > 0) {
        bot.database.userRepository.addInventoryItem(args.user, CCTItem.id, cct, clientSession)
      }
      clientSession.commitTransactionAndAwait()
    }
    sendVoteNotification(args.user)
    ReminderUtils.sendVoteReminder(userData)
  }

  suspend fun ApplicationCall.respond(statusCode: HttpStatusCode) {
    respondText(statusCode.toString(), status = statusCode)
  }

  fun start() {
    val port = System.getenv("HTTP_PORT")?.toIntOrNull() ?: 9999
    embeddedServer(Jetty, port = port) {
      install(ContentNegotiation) {
        json(
          Json {
            ignoreUnknownKeys = true
            serializersModule = IdKotlinXSerializationModule
          }
        )
      }

      val basicAuthUsername = System.getenv("BASIC_AUTH_USERNAME")
      val basicAuthPassword = System.getenv("BASIC_AUTH_PASSWORD")
      if (basicAuthUsername != null && basicAuthPassword != null) {
        install(Authentication) {
          basic("private-api") {
            realm = "Private API"
            validate { credentials ->
              if (credentials.name == basicAuthUsername && credentials.password == basicAuthPassword) {
                UserIdPrincipal(credentials.name)
              } else null
            }
          }
        }
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
                if (!`package`.giveReward(bot, userData, item)) {
                  call.respond(HttpStatusCode.NotAcceptable, "Transaction cancelled")
                  return@get
                }
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
                      if (!`package`.giveReward(bot, userData, item)) {
                        call.respond(HttpStatusCode.NotAcceptable, "Transaction cancelled")
                        return@get
                      }
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

                  sendDonationNotification(
                    order.userId, order.price.toString(), itemName, orderInfo.status.toString()
                  )
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

        post("/api/donatebot") {
          val secret = call.request.header("Authorization")
          if (secret != donateBotSecret) {
            call.respond(HttpStatusCode.Unauthorized)
            return@post
          }

          val donateBotTransaction = call.receive<DonateBotTransaction>()
          bot.database.donateBotTransactionRepository.createTransaction(donateBotTransaction)

          if (donateBotTransaction.rawBuyerId.isNullOrEmpty()) {
            // TODO: send anonymous donation notification?
            call.respond(HttpStatusCode.OK)
            return@post
          }

          try {
            var itemName = "Unknown"
            val userData = bot.database.userRepository.getUser(donateBotTransaction.rawBuyerId)

            if (donateBotTransaction.status == DonateBotTransaction.Status.COMPLETED) {
              val packageId = if (!donateBotTransaction.roleId.isNullOrEmpty()) "roles" else "gems"
              val itemId = donateBotTransaction.roleId ?: donateBotTransaction.productId ?: throw IllegalStateException(
                "User must be purchasing either a role or a product..."
              )
              val `package` = Package.packages.find {
                it.id == packageId
              }
              `package`?.let {
                val item = `package`.items.find {
                  it.id == itemId
                }
                item?.let {
                  itemName = I18n.translate(null, "store.packages.${`package`.id}.items.${item.i18nKey}")
                  if (!`package`.giveReward(bot, userData, item)) {
                    call.respond(HttpStatusCode.NotAcceptable, "Transaction cancelled")
                    return@post
                  }
                }
              }
            }

            call.respond(HttpStatusCode.OK)

            sendDonationNotification(
              userData.id,
              donateBotTransaction.price,
              itemName,
              donateBotTransaction.status.toString(),
              privateOnly = donateBotTransaction.status != DonateBotTransaction.Status.COMPLETED,
            )
          } catch (e: Throwable) {
            e.printStackTrace()
          }
        }

        authenticate("private-api") {
          get("/api/users/{userId?}/purchase-history") {
            val userId = call.parameters["userId"]
            if (userId == null) {
              call.respond(HttpStatusCode.BadRequest)
            } else {
              val orders = bot.database.orderRepository.getOrdersByUser(userId)
              call.respond(orders)
            }
          }

          get("/api/users/{userId?}/reset-credits") {
            val userId = call.parameters["userId"]
            if (userId == null) {
              call.respond(HttpStatusCode.BadRequest)
            } else {
              val userData = bot.database.userRepository.getUser(userId)
              bot.database.userRepository.incCredits(userData, -userData.credits + 1000)
              call.respond(HttpStatusCode.OK)
            }
          }

          get("/api/users/{userId?}/blacklisted") {
            val userId = call.parameters["userId"]
            if (userId == null) {
              call.respond(HttpStatusCode.BadRequest)
            } else {
              val userData = bot.database.userRepository.getUser(userId)
              call.respond(HttpStatusCode.OK, userData.blacklisted)
            }
          }

          get("/api/users/{userId?}/blacklist") {
            val userId = call.parameters["userId"]
            if (userId == null) {
              call.respond(HttpStatusCode.BadRequest)
            } else {
              val userData = bot.database.userRepository.getUser(userId)
              if (userData.blacklisted) {
                call.respond(HttpStatusCode.NotModified)
              } else {
                bot.database.userRepository.setBlacklisted(userData, true)
                call.respond(HttpStatusCode.OK)
              }
            }
          }

          get("/api/users/{userId?}/whitelist") {
            val userId = call.parameters["userId"]
            if (userId == null) {
              call.respond(HttpStatusCode.BadRequest)
            } else {
              val userData = bot.database.userRepository.getUser(userId)
              if (!userData.blacklisted) {
                call.respond(HttpStatusCode.NotModified)
              } else {
                bot.database.userRepository.setBlacklisted(userData, false)
                call.respond(HttpStatusCode.OK)
              }
            }
          }
        }

        get("/_/internal/ready") {
          try {
            if (bot.shardManager.statuses.isNotEmpty() && bot.shardManager.statuses.all { it.value == JDA.Status.CONNECTED }) {
              return@get call.respond(HttpStatusCode.OK)
            }
          } catch (_: UninitializedPropertyAccessException) {
          }
          call.respond(HttpStatusCode.ServiceUnavailable)
        }
      }
    }.start()
  }
}
