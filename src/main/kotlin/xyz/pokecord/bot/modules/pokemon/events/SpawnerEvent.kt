package xyz.pokecord.bot.modules.pokemon.events

import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import org.slf4j.LoggerFactory
import xyz.pokecord.bot.core.managers.database.models.SpawnChannel
import xyz.pokecord.bot.core.structures.discord.MessageCommandContext
import xyz.pokecord.bot.core.structures.discord.SpawnChannelMutex
import xyz.pokecord.bot.core.structures.discord.base.Event
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.utils.extensions.awaitSuspending
import kotlin.random.Random

class SpawnerEvent : Event() {
  override val name = "Spawner"

  private val logger = LoggerFactory.getLogger(this::class.java)

  private val envFlag = System.getenv("SPAWNS") != null

  private fun getNextSpawn(): Int {
    var pokemonId = Random.nextInt(1, 808)
    when {
      Pokemon.legendaries.contains(pokemonId) -> {
        if (Random.nextDouble() * 100 > 6.36) pokemonId = Random.nextInt(1, 808)
      }
      Pokemon.mythicals.contains(pokemonId) -> {
        if (Random.nextDouble() * 100 > 2.34) pokemonId = Random.nextInt(1, 808)
      }
    }
    return pokemonId
  }

  @Handler
  suspend fun onMessage(context: MessageCommandContext) {
    try {
      if (!context.shouldProcess()) return
      if (!envFlag || context.bot.maintenance) return
      if (context.event.channelType != ChannelType.TEXT) return
      val prefix = context.getPrefix()
      if (context.event.message.contentRaw.startsWith(prefix)) return
      val spawnChannels = module.bot.database.spawnChannelRepository.getSpawnChannels(context.guild!!.id)
      var randomSpawnChannel = spawnChannels.randomOrNull() ?: return
      SpawnChannelMutex[randomSpawnChannel.id].withLock {
        randomSpawnChannel = module.bot.database.spawnChannelRepository.getSpawnChannel(randomSpawnChannel.id) ?: return
        val randomSpawnChannelEntity = context.guild!!.getTextChannelById(randomSpawnChannel.id) ?: return
        if (!randomSpawnChannelEntity.guild.selfMember.hasPermission(
            randomSpawnChannelEntity,
            Permission.MESSAGE_WRITE,
            Permission.MESSAGE_EMBED_LINKS
          )
        ) return // TODO: maybe let them know about missing permission somehow?
        val lastMessageAt = context.bot.cache.lastCountedMessageMap.getAsync(context.author.id).awaitSuspending()
        val now = System.currentTimeMillis()
        if (lastMessageAt != null && lastMessageAt + 5000 > now) return
        context.bot.cache.lastCountedMessageMap.replaceAsync(context.author.id, now).awaitSuspending()

        val oldSpawnChannelData = SpawnChannel(
          randomSpawnChannel.id,
          randomSpawnChannel.guildId,
          randomSpawnChannel.requiredMessages,
          randomSpawnChannel.sentMessages,
          randomSpawnChannel.spawned
        )

        randomSpawnChannel.sentMessages++
        if (randomSpawnChannel.sentMessages >= randomSpawnChannel.requiredMessages) {
          try {
            randomSpawnChannel.sentMessages = 0
            randomSpawnChannel.requiredMessages = Random.nextInt(5, 41)
            randomSpawnChannel.spawned = getNextSpawn()

            module.bot.database.spawnChannelRepository.updateDetails(
              randomSpawnChannel
            )
            val embed = context.embedTemplates.normal(
              context.translate(
                "modules.pokemon.events.spawner.embeds.spawn.description",
                "prefix" to prefix
              ),
              context.translate("modules.pokemon.events.spawner.embeds.spawn.title")
            )
              .setFooter(context.translate("modules.pokemon.events.spawner.embeds.spawn.footer"))
              .setImage(Pokemon.getImageUrl(randomSpawnChannel.spawned))
              .also {
                Pokemon.getById(randomSpawnChannel.spawned)?.species?.color?.let { color ->
                  it.setColor(color.colorCode)
                }
              }
              .build()
            randomSpawnChannelEntity.sendMessage(embed).queue()
          } catch (e: Exception) {
            logger.error("Spawn error", e)
            // Undo the changes we made to the spawn channel since there was an error spawning
            module.bot.database.spawnChannelRepository.updateDetails(
              oldSpawnChannelData
            )
          }
        } else {
          module.bot.database.spawnChannelRepository.updateMessageCount(randomSpawnChannel)
        }
      }
    } catch (e: Exception) {
      context.handleException(e, module, event = this)
    }
  }
}
