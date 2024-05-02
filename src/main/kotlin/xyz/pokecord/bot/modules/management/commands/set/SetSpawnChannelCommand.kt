package xyz.pokecord.bot.modules.management.commands.set

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.SpawnChannel
import xyz.pokecord.bot.core.structures.discord.base.Command
import kotlin.random.Random

object SetSpawnChannelCommand : Command() {
  override val name = "Spawn"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument textChannel: TextChannel?
  ) {
    if (!context.isFromGuild) {
      context.reply(
        context.translate("misc.errors.serverOnlyCommand")
      ).queue()
      return
    }
    if (!context.guild!!.retrieveMember(context.author).await().hasPermission(Permission.ADMINISTRATOR)) {
      // TODO: say you're not admin PROPERLY
      context.reply(
        context.embedTemplates.error("Please ask a server administrator to use this command instead.").build()
      ).queue()
      return
    }

    val spawnChannels =
      module.bot.database.spawnChannelRepository.getSpawnChannels(context.guild!!.id).toMutableList()

    val removed = spawnChannels.removeAll {
      context.guild!!.getTextChannelById(it.id) == null
    }

    if (removed) {
      module.bot.database.spawnChannelRepository.setSpawnChannels(context.guild!!.id, spawnChannels)
    }

    if (textChannel == null) {
      context.reply(
        context.embedTemplates.normal(
          if (spawnChannels.isEmpty()) context.translate("modules.general.commands.set.spawn.noChannelsSet")
          else spawnChannels.joinToString(
            "\n"
          ) { "<#${it.id}>" },
          context.translate("modules.general.commands.set.spawn.list.title")
        ).build()
      ).queue()
      return
    }
    val existingSpawnChannel = module.bot.database.spawnChannelRepository.getSpawnChannel(textChannel.id)
    if (existingSpawnChannel != null) {
      module.bot.database.spawnChannelRepository.removeSpawnChannel(existingSpawnChannel)
    } else {
      module.bot.database.spawnChannelRepository.setSpawnChannel(
        SpawnChannel(
          textChannel.id,
          textChannel.guild.id,
          Random.nextInt(10, 16),
          0,
          0
        )
      )
    }
    context.reply(
      context.embedTemplates.normal(
        context.translate(
          "modules.general.commands.set.spawn.description", mapOf(
            "channelName" to textChannel.name,
            "channelMention" to textChannel.asMention,
            "channelId" to textChannel.id
          )
        ),
        context.translate(if (existingSpawnChannel == null) "modules.general.commands.set.spawn.title.added" else "modules.general.commands.set.spawn.title.removed")
      ).build()
    ).queue()
  }
}