package xyz.pokecord.bot.modules.pokemon.commands

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.utils.extensions.awaitSuspending
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CatchCommand : Command() {
  override val name = "Catch"

  override var aliases = arrayOf("c")
  override var rateLimit = 5000L
  override var rateLimitType = RateLimitType.Args

  override fun getRateLimitCacheKey(context: ICommandContext, args: List<String>): String {
    val arg = args.joinToString(" ")
    val pokemon = Pokemon.getByName(arg)
    return (if (rateLimitType == RateLimitType.Command) "${context.author.id}.${module.name}.${name}" else "${context.author.id}.${module.name}.${name}.${
      pokemon?.name ?: arg
    }").toLowerCase()
  }

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(consumeRest = true, name = "pokemon") pokemonName: String?,
  ) {
    if (!context.hasStarted(true)) return

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

    withContext(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
      val lock = module.bot.cache.getSpawnChannelLock(spawnChannel.id)
      lock.lockAsync(5, TimeUnit.SECONDS).awaitSuspending()

      if (spawnChannel.spawned == 0) {
        context.reply(
          context.embedTemplates.error(
            context.translate(
              "misc.errors.nothingSpawned"
            )
          ).build()
        ).queue()
        return@withContext
      }

      if (pokemonName == null) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.pokemon.commands.catch.errors.noNameProvided")
          ).build()
        ).queue()
        return@withContext
      }

      val pokemon = Pokemon.getByName(pokemonName)
      if (pokemon != null && spawnChannel.spawned == pokemon.id) {
        val ownedPokemon = module.bot.database.userRepository.givePokemon(context.getUserData(), pokemon.id) {
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
        ).queue()
      } else {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.pokemon.commands.catch.errors.incorrectNameProvided")
          ).build()
        ).queue()
        return@withContext
      }
      lock.unlockAsync().awaitSuspending()
    }
  }
}
