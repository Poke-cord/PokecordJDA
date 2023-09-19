package xyz.pokecord.discord4j.connect.rsocket.servers

import discord4j.connect.rsocket.gateway.RSocketPayloadServer
import discord4j.connect.rsocket.global.RSocketGlobalRouterServer
import discord4j.connect.rsocket.shard.RSocketShardCoordinatorServer
import discord4j.rest.request.BucketGlobalRateLimiter
import discord4j.rest.request.RequestQueueFactory
import io.rsocket.transport.netty.server.CloseableChannel
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.util.Logger
import reactor.util.Loggers
import reactor.util.retry.Retry
import java.net.InetSocketAddress
import java.time.Duration


object Main {
  private val log: Logger = Loggers.getLogger(Main::class.java)

  private fun startGlobalRouterServer(): Mono<Void> {
    val routerServer = RSocketGlobalRouterServer(
      InetSocketAddress(Constants.GLOBAL_ROUTER_PORT),
      BucketGlobalRateLimiter.create(), Schedulers.parallel(), RequestQueueFactory.buffering()
    )

    return routerServer.start()
      .doOnNext { cc: CloseableChannel ->
        log.info(
          "Started global router server at {}",
          cc.address()
        )
      }
      .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1)).maxBackoff(Duration.ofMinutes(1)))
      .flatMap { obj: CloseableChannel -> obj.onClose() }
  }

  private fun startShardCoordinator(): Mono<Void> {
    return RSocketShardCoordinatorServer(InetSocketAddress(Constants.SHARD_COORDINATOR_PORT))
      .start()
      .doOnNext { cc: CloseableChannel ->
        log.info(
          "Started shard coordinator server at {}",
          cc.address()
        )
      }
      .blockOptional()
      .orElseThrow { RuntimeException("Something went wrong with the RSocketShardCoordinator") }
      .onClose()
  }

  private fun startPayloadServer(): Mono<Void> {
    return RSocketPayloadServer(InetSocketAddress(Constants.PAYLOAD_PORT))
      .start()
      .doOnNext { cc: CloseableChannel ->
        log.info(
          "Started payload server at {}",
          cc.address()
        )
      }
      .blockOptional()
      .orElseThrow { RuntimeException("Something went wrong with the RSocketPayloadServer") }
      .onClose()
  }

  @JvmStatic
  fun main(args: Array<String>) {
    startGlobalRouterServer().and(startShardCoordinator()).and(startPayloadServer()).block()
  }
}