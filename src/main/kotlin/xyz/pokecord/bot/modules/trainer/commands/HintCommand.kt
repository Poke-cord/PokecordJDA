package xyz.pokecord.bot.modules.trainer.commands

import dev.minn.jda.ktx.await
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.utils.Confirmation

class HintCommand : Command() {
  override val name = "Hint"

  @Executor
  suspend fun execute(
    context: ICommandContext
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

    val userData = context.getUserData()
    if (userData.credits < 10) {
      context.reply(
        context.embedTemplates.normal(
          context.translate("modules.pokemon.commands.hint.errors.notEnoughCredits")
        ).build()
      ).queue()
      return
    }

    val confirmation = Confirmation(context)
    val result =
      confirmation.result(
        context.embedTemplates.confirmation(
          context.translate(
            "modules.pokemon.commands.hint.confirmation.embed.description",
            "user" to context.author.asMention
          ),
          context.translate("modules.pokemon.commands.hint.confirmation.embed.title")
        )
          .setFooter(
            context.translate("misc.confirmation.timeoutFooter", "timeout" to confirmation.timeout.toString())
          )
      )
    if (!result) {
      context.reply(
        context.embedTemplates.normal(
          context.translate("modules.pokemon.commands.hint.errors.cancelled.description"),
          context.translate("modules.pokemon.commands.hint.errors.cancelled.title")
        ).build()
      ).queue()
      return
    }

    val pokemon = Pokemon.getById(spawnChannel.spawned)
    pokemon?.let {
      val name = it.name
      try {
        val privateChannel = context.author.openPrivateChannel().submit().await()
        privateChannel.sendMessageEmbeds(
          context.embedTemplates.normal(
            "```${name.take(1)} ${"_ ".repeat(name.length - 1)}```",
            context.translate("modules.pokemon.commands.hint.dm.embed.title")
          ).build()
        ).await()
        module.bot.database.userRepository.incCredits(userData, -10)
        context.reply(
          context.embedTemplates.success(
            context.translate("modules.pokemon.commands.hint.embed.description"),
            context.translate("modules.pokemon.commands.hint.embed.title")
          ).build()
        ).queue()
      } catch (e: Exception) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.pokemon.commands.hint.errors.dmFailed")
          ).build()
        ).queue()
      }
    }
  }
}
