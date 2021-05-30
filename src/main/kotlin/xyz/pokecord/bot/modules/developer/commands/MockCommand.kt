package xyz.pokecord.bot.modules.developer.commands

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.internal.entities.ReceivedMessage
import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext
import xyz.pokecord.bot.modules.developer.DeveloperCommand

class MockCommand : DeveloperCommand() {
  override val name = "Mock"

  @Executor
  suspend fun execute(
    context: MessageReceivedContext,
    @Argument(name = "target user") targetUser: User?,
    @Argument(name = "command", consumeRest = true) command: String?
  ) {
    if (targetUser == null || command == null) return
    if (context.isFromGuild && context.guild.selfMember.hasPermission(
        context.channel as GuildChannel,
        Permission.MESSAGE_MANAGE
      )
    ) {
      context.message.delete()
    }

    val sentMessage =
      context.channel.sendMessage("Executing `$command` on behalf of `${targetUser.asTag}`").await()
    val targetMember = context.guild.retrieveMember(targetUser).await() ?: null
    val fakeMessage = ReceivedMessage(
      sentMessage.idLong,
      context.channel,
      sentMessage.type,
      null,
      false,
      false,
      null,
      null,
      false,
      false,
      command,
      sentMessage.nonce,
      targetUser,
      targetMember,
      null,
      null,
      listOf(),
      listOf(),
      listOf(),
      Message.MessageFlag.toBitField(sentMessage.flags)
    )
    val fakeEvent = MessageReceivedEvent(module.bot.jda, context.responseNumber, fakeMessage)
    module.bot.jda.eventManager.handle(fakeEvent)
  }
}
