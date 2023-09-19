package xyz.pokecord.d4j.connect.leader

import discord4j.common.JacksonResources
import discord4j.common.store.Store
import discord4j.common.store.legacy.LegacyStoreLayout
import discord4j.connect.common.ConnectGatewayOptions
import discord4j.connect.common.DownstreamGatewayClient
import discord4j.connect.rsocket.gateway.RSocketJacksonSinkMapper
import discord4j.connect.rsocket.gateway.RSocketJacksonSourceMapper
import discord4j.connect.rsocket.gateway.RSocketPayloadSink
import discord4j.connect.rsocket.gateway.RSocketPayloadSource
import discord4j.connect.rsocket.global.RSocketGlobalRateLimiter
import discord4j.connect.rsocket.router.RSocketRouter
import discord4j.connect.rsocket.router.RSocketRouterOptions
import xyz.pokecord.d4j.connect.worker.BotSupport
import discord4j.core.DiscordClient
import discord4j.core.shard.MemberRequestFilter
import discord4j.core.shard.ShardingStrategy
import discord4j.store.api.readonly.ReadOnlyStoreService
import discord4j.store.redis.RedisStoreService
import io.lettuce.core.RedisClient
import java.net.InetSocketAddress


object Main {
  @JvmStatic
  fun main(args: Array<String>) {
    val discordToken = System.getenv("DISCORD_TOKEN") ?: throw IllegalStateException("Discord token is required.")

    val globalRouterServerAddress =
      InetSocketAddress(Constants.GLOBAL_ROUTER_HOST, Constants.GLOBAL_ROUTER_PORT)
    val payloadServerAddress = InetSocketAddress(Constants.PAYLOAD_HOST, Constants.PAYLOAD_PORT)

    val jackson = JacksonResources.create()

    val redisClient: RedisClient = RedisClient.create(Constants.REDIS_URL)

    val singleStrategy = ShardingStrategy.single()

    val client = DiscordClient.builder(discordToken)
      .setJacksonResources(jackson)
      .setGlobalRateLimiter(RSocketGlobalRateLimiter.createWithServerAddress(globalRouterServerAddress))
      .setExtraOptions {
        RSocketRouterOptions(it) { globalRouterServerAddress }
      }
      .build(::RSocketRouter)
      .gateway()
      .setSharding(singleStrategy)
      .setMemberRequestFilter(MemberRequestFilter.none())
      .setStore(
        Store.fromLayout(
          LegacyStoreLayout.of(
            ReadOnlyStoreService(
              RedisStoreService.builder()
                .redisClient(redisClient)
                .useSharedConnection(false)
                .build()
            )
          )
        )
      )
      .setExtraOptions {
        ConnectGatewayOptions(
          it,
          RSocketPayloadSink(payloadServerAddress, RSocketJacksonSinkMapper(jackson.objectMapper, "outbound")),
          RSocketPayloadSource(
            payloadServerAddress,
            "inbound",
            RSocketJacksonSourceMapper(jackson.objectMapper)
          )
        )
      }
      .login(::DownstreamGatewayClient)
      .blockOptional()
      .orElseThrow {
        RuntimeException("Something went wrong with worker client.")
      }

    BotSupport.create(client).eventHandlers().block()

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