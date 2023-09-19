package xyz.pokecord.d4j.connect.leader

import discord4j.common.JacksonResources
import discord4j.common.store.Store
import discord4j.common.store.legacy.LegacyStoreLayout
import discord4j.connect.common.ConnectGatewayOptions
import discord4j.connect.common.UpstreamGatewayClient
import discord4j.connect.rsocket.gateway.RSocketJacksonSinkMapper
import discord4j.connect.rsocket.gateway.RSocketJacksonSourceMapper
import discord4j.connect.rsocket.gateway.RSocketPayloadSink
import discord4j.connect.rsocket.gateway.RSocketPayloadSource
import discord4j.connect.rsocket.global.RSocketGlobalRateLimiter
import discord4j.connect.rsocket.router.RSocketRouter
import discord4j.connect.rsocket.router.RSocketRouterOptions
import discord4j.connect.rsocket.shard.RSocketShardCoordinator
import discord4j.core.DiscordClient
import discord4j.core.event.dispatch.DispatchEventMapper
import discord4j.core.shard.ShardingStrategy
import discord4j.store.redis.RedisStoreService
import io.lettuce.core.RedisClient
import java.net.InetSocketAddress


object Main {
  @JvmStatic
  fun main(args: Array<String>) {
    val discordToken = System.getenv("DISCORD_TOKEN") ?: throw IllegalStateException("Discord token is required.")

    val globalRouterServerAddress =
      InetSocketAddress(Constants.GLOBAL_ROUTER_HOST, Constants.GLOBAL_ROUTER_PORT)
    val coordinatorServerAddress =
      InetSocketAddress(Constants.SHARD_COORDINATOR_HOST, Constants.SHARD_COORDINATOR_PORT)
    val payloadServerAddress = InetSocketAddress(Constants.PAYLOAD_HOST, Constants.PAYLOAD_PORT)

    val jackson = JacksonResources.create()

    val redisClient: RedisClient = RedisClient.create(Constants.REDIS_URL)

    val recommendedStrategy = ShardingStrategy.recommended()

    val client = DiscordClient.builder(discordToken)
      .setJacksonResources(jackson)
      .setGlobalRateLimiter(RSocketGlobalRateLimiter.createWithServerAddress(globalRouterServerAddress))
      .setExtraOptions {
        RSocketRouterOptions(it) { globalRouterServerAddress }
      }
      .build(::RSocketRouter)
      .gateway()
      .setSharding(recommendedStrategy)
      .setShardCoordinator(RSocketShardCoordinator.createWithServerAddress(coordinatorServerAddress))
      .setStore(
        Store.fromLayout(
          LegacyStoreLayout.of(
            RedisStoreService.builder()
              .redisClient(redisClient)
              .useSharedConnection(false)
              .build()
          )
        )
      )
      .setDispatchEventMapper(DispatchEventMapper.discardEvents())
      .setExtraOptions {
        ConnectGatewayOptions(
          it,
          RSocketPayloadSink(payloadServerAddress, RSocketJacksonSinkMapper(jackson.objectMapper, "inbound")),
          RSocketPayloadSource(
            payloadServerAddress,
            "outbound",
            RSocketJacksonSourceMapper(jackson.objectMapper)
          )
        )
      }
      .login(::UpstreamGatewayClient)
      .blockOptional()
      .orElseThrow {
        RuntimeException("Something went wrong with leader client.")
      }

    client.onDisconnect().block()

//    DiscordClient.create(discordToken)
//      .withGateway { gateway ->
//        val printOnLogin = gateway.on(ReadyEvent::class.java) { event ->
//          Mono.fromRunnable<Void> {
//            val self = event.self
//            System.out.printf("Logged in as %s#%s%n", self.username, self.discriminator)
//          }
//        }.then()
//
//        val handlePingCommand = gateway.on(MessageCreateEvent::class.java) { event ->
//          val message = event.message
//          if (message.content.equals("!ping", ignoreCase = true)) {
//            return@on message.channel
//              .flatMap { channel: MessageChannel ->
//                channel.createMessage(
//                  "pong!"
//                )
//              }
//          }
//          Mono.empty()
//        }.then()
//
//        return@withGateway printOnLogin.and(handlePingCommand)
//      }.block()
  }
}