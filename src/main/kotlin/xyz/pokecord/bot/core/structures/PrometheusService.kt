package xyz.pokecord.bot.core.structures

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.PushGateway
import java.net.MalformedURLException
import java.net.URL
import kotlin.concurrent.thread

object PrometheusService {
  private var pushGateway: PushGateway? = null

  val registry = CollectorRegistry()

  init {
    val pushGatewayUrl = System.getenv("PROM_PUSH_GATEWAY_URL")?.let {
      try {
        URL(it)
      } catch (e: MalformedURLException) {
        null
      }
    }
    if (pushGatewayUrl != null) {
      pushGateway = PushGateway(pushGatewayUrl)
      thread {
        while (true) {
          try {
            pushGateway?.pushAdd(registry, "pokecord-bot")
          } catch (e: Throwable) {
            e.printStackTrace()
          }
          Thread.sleep(30_000)
        }
      }
    }
  }
}
