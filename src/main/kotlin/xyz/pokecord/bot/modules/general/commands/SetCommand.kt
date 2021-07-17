package xyz.pokecord.bot.modules.general.commands

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.SpawnChannel
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import kotlin.random.Random

class SetCommand : ParentCommand() {
  override val name = "Set"

  override var aliases = arrayOf("settings", "setting", "config")

  @ChildCommand
  class SilenceCommand : Command() {
    override val name = "Silence"

    @Executor
    suspend fun execute(context: ICommandContext) {
      val guildData = context.getGuildData() ?: return
      module.bot.database.guildRepository.toggleSilence(guildData)
      context.reply(
        context.embedTemplates.normal(
          context.translate(if (!guildData.levelUpMessagesSilenced) "modules.general.commands.set.silence.enabled" else "modules.general.commands.set.silence.disabled"),
          context.translate("modules.general.commands.set.silence.title")
        ).build()
      ).queue()
    }
  }

  @ChildCommand
  class SetPrefixCommand : Command() {
    override val name = "Prefix"

    override var requiredUserPermissions = arrayOf(Permission.ADMINISTRATOR)

    @Executor
    suspend fun execute(
      context: ICommandContext,
      @Argument prefix: String?
    ) {
      if (!context.isFromGuild) {
        context.reply(
          context.translate("misc.texts.serverOnlyCommand")
        ).queue()
        return
      }

      if (prefix == null) {
        context.reply(
          context.embedTemplates.normal(
            context.translate("modules.general.commands.set.prefix.current", "prefix" to context.getPrefix())
          ).build()
        ).queue()
        return
      }

      if (prefix.length > 10) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.general.commands.set.prefix.tooLarge")
          ).build()
        ).queue()
        return
      }

      module.bot.database.guildRepository.setPrefix(context.getGuildData()!!, prefix)
      context.reply(
        context.embedTemplates.normal(
          context.translate("modules.general.commands.set.prefix.updated", "prefix" to prefix)
        ).build()
      ).queue()
    }
  }

  @ChildCommand
  class SetPrivateCommand : Command() {
    override val name = "Private"

    @Executor
    suspend fun execute(context: ICommandContext) {
      val userData = context.getUserData()
      module.bot.database.userRepository.togglePrivate(userData)
      context.reply(
        context.embedTemplates.normal(
          context.translate(if (userData.progressPrivate) "modules.general.commands.set.private.enabled" else "modules.general.commands.set.private.disabled"),
          context.translate("modules.general.commands.set.private.title")
        ).build()
      ).queue()
    }
  }

  @ChildCommand
  class SetSpawnChannelCommand : Command() {
    override val name = "Spawn"

    @Executor
    suspend fun execute(
      context: ICommandContext,
      @Argument textChannel: TextChannel?
    ) {
      if (!context.isFromGuild) {
        context.reply(
          context.translate("misc.texts.serverOnlyCommand")
        ).queue()
        return
      }
      if (context.guild?.retrieveMember(context.author)?.await()?.hasPermission(Permission.ADMINISTRATOR) != true) {
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
            Random.nextInt(5, 41),
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
}
