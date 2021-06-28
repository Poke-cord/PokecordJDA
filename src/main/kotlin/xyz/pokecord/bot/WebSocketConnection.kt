package xyz.pokecord.bot

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import xyz.pokecord.bot.core.managers.I18n
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.store.packages.Package
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.api.PayPal
import kotlin.concurrent.thread

class WebSocketConnection(
  private val address: String,
  val bot: Bot
) {

  @Serializable
  data class OrderMessage(
    val id: String,
    val type: String
  )

  @Serializable
  data class VoteMessage(
    val userId: String,
    val type: String
  )

  @Serializable
  data class OrderFailedResponseMessage(
    val orderId: String,
    val code: Int,
    val message: String,
    val type: String
  )

  @Serializable
  data class OrderSuccessResponseMessage(
    val orderId: String,
    val paid: Boolean,
    val itemName: String,
    val price: Double,
    val username: String,
    val userId: String,
    val status: PayPal.OrderStatus,
    val type: String
  )

  private var httpClient = HttpClient {
    install(WebSockets)
  }

  init {
    thread {
      runBlocking {
        httpClient.ws(address) {
          for (frame in incoming) {
            if (frame is Frame.Text) {
              try {
                val json = Json.parseToJsonElement(frame.readText())
                if (json is JsonObject) {
                  val messageType = json["type"]
                  if (messageType is JsonPrimitive && messageType.isString) {
                    if (messageType.content == "order") {
                      val orderMessage = Json.decodeFromJsonElement<OrderMessage>(json)
                      val order = bot.database.orderRepository.getOrder(orderMessage.id)
                      if (order == null) {
                        outgoing.send(
                          Frame.Text(
                            Json.encodeToString(
                              OrderFailedResponseMessage(
                                orderMessage.id,
                                404,
                                "The order ID provided is invalid and could not be found in the database.",
                                "orderError"
                              )
                            )
                          )
                        )
                      } else {
                        try {
                          var orderInfo = bot.payPal.getOrderInfo(order.orderId)
                          if (orderInfo.status == PayPal.OrderStatus.APPROVED) {
                            orderInfo = bot.payPal.captureOrder(order.orderId)
                          }
                          if (orderInfo.status != PayPal.OrderStatus.COMPLETED) {
                            outgoing.send(
                              Frame.Text(
                                Json.encodeToString(
                                  OrderFailedResponseMessage(
                                    orderMessage.id,
                                    404,
                                    "The order with id ${order.orderId} is not paid yet, try again later. Status: ${orderInfo.status}",
                                    "orderError"
                                  )
                                )
                              )
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

                              outgoing.send(
                                Frame.Text(
                                  Json.encodeToString(
                                    OrderSuccessResponseMessage(
                                      order.orderId,
                                      order.paid,
                                      itemName,
                                      order.price,
                                      order.userName,
                                      order.userId,
                                      orderInfo.status!!,
                                      "orderResult"
                                    )
                                  )
                                )
                              )
                            } else {
                              outgoing.send(
                                Frame.Text(
                                  Json.encodeToString(
                                    OrderFailedResponseMessage(
                                      orderMessage.id,
                                      303,
                                      "This order has already been paid.",
                                      "orderError"
                                    )
                                  )
                                )
                              )
                            }
                          }
                        } catch (e: Throwable) {
                          e.printStackTrace()
                        }
                      }
                    } else if (messageType.content == "vote") {
                      try {
                        val voteMessage = Json.decodeFromJsonElement<VoteMessage>(json)
                        println(voteMessage.userId)
                      } catch (e: Throwable) {
                        e.printStackTrace()
                      }
                    }
                  }
                }
              } catch (e: SerializationException) {
                e.printStackTrace()
              }
            } else {
              println(frame)
            }
          }
        }
      }
    }
  }
}
