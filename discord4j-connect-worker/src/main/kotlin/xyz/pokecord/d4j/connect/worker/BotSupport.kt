package xyz.pokecord.d4j.connect.worker

import discord4j.common.util.Snowflake
import discord4j.connect.support.EventHandler
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.`object`.presence.ClientPresence
import discord4j.core.spec.legacy.LegacyEmbedCreateSpec
import discord4j.discordjson.json.ApplicationInfoData
import discord4j.discordjson.json.ImmutableMessageCreateRequest
import discord4j.discordjson.json.UserData
import discord4j.discordjson.possible.Possible
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.util.Loggers
import java.util.stream.Collectors

class BotSupport internal constructor(private val client: GatewayDiscordClient) {
  fun eventHandlers(): Mono<Void> {
    return Mono.`when`(
      readyHandler(client), commandHandler(
        client
      )
    )
  }

  class Echo : EventHandler() {
    override fun onMessageCreate(event: MessageCreateEvent): Mono<Void> {
      val message = event.message
      val content = message.content
      return if (content.startsWith("!echo ")) {
        message.restChannel.createMessage(
          ImmutableMessageCreateRequest.builder()
            .content(Possible.of("<@" + message.userData.id() + "> " + content.substring("!echo ".length)))
            .build()
        )
          .then()
      } else Mono.empty()
    }
  }

  class StatusEmbed : EventHandler() {
    override fun onMessageCreate(event: MessageCreateEvent): Mono<Void> {
      val message = event.message
      return Mono.justOrEmpty(message.content)
        .filter { content: String -> content == "!status" }
        .flatMap { source: String? ->
          message.channel
            .publishOn(Schedulers.boundedElastic())
            .flatMap { channel: MessageChannel ->
              channel.createEmbed { spec: LegacyEmbedCreateSpec ->
                spec.setThumbnail(
                  event.client.self
                    .blockOptional()
                    .orElseThrow { RuntimeException() }
                    .avatarUrl
                )
                spec.addField(
                  "Servers", event.client.guilds.count()
                    .blockOptional()
                    .orElse(-1L)
                    .toString(), false
                )
                spec.addField(
                  "Application-Info", event.client.applicationInfo
                    .blockOptional()
                    .orElseThrow { RuntimeException() }
                    .toString(), false
                )
              }
            }
        }
        .then()
    }
  }

  class Status : EventHandler() {
    override fun onMessageCreate(event: MessageCreateEvent): Mono<Void> {
      val message = event.message
      return Mono.justOrEmpty(message.content)
        .filter { content: String -> content.startsWith("!status ") }
        .map { content: String ->
          val status = content.substring("!status ".length)
          if (status.equals("online", ignoreCase = true)) {
            return@map ClientPresence.online()
          } else if (status.equals("dnd", ignoreCase = true)) {
            return@map ClientPresence.doNotDisturb()
          } else if (status.equals("idle", ignoreCase = true)) {
            return@map ClientPresence.idle()
          } else if (status.equals("invisible", ignoreCase = true)) {
            return@map ClientPresence.invisible()
          } else {
            throw IllegalArgumentException("Invalid argument")
          }
        }
        .flatMap { presence: ClientPresence? ->
          event.client.updatePresence(presence)
        }
        .then()
    }
  }

  class Exit : EventHandler() {
    override fun onMessageCreate(event: MessageCreateEvent): Mono<Void> {
      val message = event.message
      return Mono.justOrEmpty(message.content)
        .filter { content: String -> content == "!exit" }
        .flatMap { presence: String? ->
          event.client.logout()
        }
        .then()
    }
  }

  companion object {
    private val log = Loggers.getLogger(BotSupport::class.java)
    fun create(client: GatewayDiscordClient): BotSupport {
      return BotSupport(client)
    }

    fun readyHandler(client: GatewayDiscordClient): Mono<Void> {
      return client.on(ReadyEvent::class.java)
        .doOnNext { ready: ReadyEvent ->
          log.info(
            "Logged in as {}",
            ready.self.username
          )
        }
        .then()
    }

    fun commandHandler(client: GatewayDiscordClient): Mono<Void> {
      val ownerId = client.rest().applicationInfo
        .map { obj: ApplicationInfoData -> obj.owner() }
        .map { user: UserData ->
          Snowflake.asLong(
            user.id()
          )
        }
        .cache()
      val eventHandlers: MutableList<EventHandler> = ArrayList()
      eventHandlers.add(Echo())
      eventHandlers.add(Status())
      eventHandlers.add(StatusEmbed())
      eventHandlers.add(Exit())
      return client.on(
        MessageCreateEvent::class.java
      ) { event: MessageCreateEvent ->
        ownerId.filter { owner: Long ->
          val author = event.message.author
            .map { obj: User -> obj.id }
            .map { obj: Snowflake -> obj.asLong() }
            .orElse(null)
          owner == author
        }
          .flatMap { id: Long? ->
            Mono.`when`(
              eventHandlers.stream()
                .map { handler: EventHandler ->
                  handler.onMessageCreate(
                    event
                  )
                }
                .collect(
                  Collectors.toList()
                )
            )
          }
      }
        .then()
    }
  }
}