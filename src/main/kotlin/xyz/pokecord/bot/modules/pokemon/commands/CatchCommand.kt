package xyz.pokecord.bot.modules.pokemon.commands

import io.prometheus.client.Counter
import kotlinx.coroutines.sync.withLock
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.PrometheusService
import xyz.pokecord.bot.core.structures.discord.SpawnChannelMutex
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.core.structures.pokemon.SpecialEvents
import kotlin.random.Random

class CatchCommand : Command() {
  override val name = "Catch"

  override var aliases = arrayOf("c")
  override var rateLimit = 4500L
  override var rateLimitType = RateLimitType.Args

  private val caught = Counter
    .build("bot_commands_catch_catches", "Total number of Pokémon caught using the Catch Command.")
    .labelNames("hostname", "user_id", "user_tag", "shard", "guild", "channel")
    .register(PrometheusService.registry)

  override fun getRateLimitCacheKey(context: ICommandContext, args: List<String>): String {
    val arg = args.joinToString(" ")
    val pokemon = Pokemon.getByName(arg)
    return (if (rateLimitType == RateLimitType.Command) "${context.author.id}.${module.name}.${name}" else "${context.author.id}.${module.name}.${name}.${
      pokemon?.name ?: arg
    }").lowercase()
  }

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(consumeRest = true, name = "pokemon") pokemonName: String?,
  ) {
    if (!context.hasStarted(true)) return

    SpawnChannelMutex[context.channel.id].withLock {
      val spawnChannel = module.bot.database.spawnChannelRepository.getSpawnChannel(context.channel.id)
      if (spawnChannel == null) {
        context.reply(
          context.embedTemplates.error(
            context.translate(
              "misc.errors.notASpawnChannel"
            )
          ).build()
        ).queue()
        return
      }

      if (spawnChannel.spawned == 0) {
        context.reply(
          context.embedTemplates.error(
            context.translate(
              "misc.errors.nothingSpawned"
            )
          ).build()
        ).queue()
        return
      }

      if (pokemonName == null) {
        context.reply(
          context.embedTemplates.error(
            context.translate("misc.errors.missingArguments.noPokemonName")
          ).build()
        ).queue()
        return
      }

      val pokemon = Pokemon.getByName(pokemonName)
      if (pokemon != null && spawnChannel.spawned == pokemon.id) {
        val eventPokemonId = SpecialEvents.handleCatching(pokemon.species)
        val shiny = eventPokemonId?.let {
          Random.nextInt(100) < 2
        }
        val finalPokemonId = eventPokemonId ?: pokemon.id
        val ownedPokemon =
          module.bot.database.userRepository.givePokemon(context.getUserData(), finalPokemonId, shiny = shiny) {
            module.bot.database.spawnChannelRepository.despawn(spawnChannel, it)
          }
        context.reply(
          context.embedTemplates.normal(
            context.translate(
              "modules.pokemon.commands.catch.embed.description",
              mapOf(
                "user" to context.author.asMention,
                "level" to "${ownedPokemon.level}",
                "pokemon" to ownedPokemon.displayName,
                "ivPercentage" to ownedPokemon.ivPercentage
              )
            ),
            context.translate(
              "modules.pokemon.commands.catch.embed.title"
            )
          )
            .setColor(pokemon.species.color.colorCode)
            .setFooter(context.translate("modules.pokemon.commands.catch.embed.footer"))
            .build()
        ).queue {
          caught.labels(
            module.bot.hostname,
            context.author.id,
            context.author.asTag,
            context.jda.shardInfo.shardId.toString(),
            context.guild!!.id,
            context.channel.id
          ).inc()
        }
      } else {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.pokemon.commands.catch.errors.incorrectNameProvided")
          ).build()
        ).queue()
        return
      }
    }
  }
}
