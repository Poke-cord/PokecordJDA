package xyz.pokecord.bot.modules.pokemon.commands

import kotlinx.coroutines.sync.withLock
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.SpawnChannelMutex
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.Pokemon

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
            context.translate("modules.pokemon.commands.catch.errors.noNameProvided")
          ).build()
        ).queue()
        return
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
                "pokemon" to ownedPokemon.displayName
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
        return
      }
    }
  }
}
