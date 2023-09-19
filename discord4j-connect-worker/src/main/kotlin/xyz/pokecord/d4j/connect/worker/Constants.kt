package xyz.pokecord.d4j.connect.leader

import java.net.InetAddress
import java.net.UnknownHostException

object Constants {
  private val localhost by lazy {
    try {
      InetAddress.getLocalHost().hostName
    } catch (e: UnknownHostException) {
      "0.0.0.0"
    }
  }

  val GLOBAL_ROUTER_HOST =
    System.getenv("ROUTER_HOST") ?: localhost
  val SHARD_COORDINATOR_HOST =
    System.getenv("SHARD_COORDINATOR_HOST") ?: localhost
  val PAYLOAD_HOST =
    System.getenv("PAYLOAD_HOST") ?: localhost

  val GLOBAL_ROUTER_PORT = 33331
  val SHARD_COORDINATOR_PORT = 33332
  val PAYLOAD_PORT = 33333

  val REDIS_URL = System.getenv("REDIS_URL") ?: "redis://${localhost}:6379"
}