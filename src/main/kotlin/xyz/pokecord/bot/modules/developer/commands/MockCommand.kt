package xyz.pokecord.bot.modules.developer.commands

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.internal.entities.ReceivedMessage
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.MessageCommandContext
import xyz.pokecord.bot.core.structures.discord.SlashCommandContext
import xyz.pokecord.bot.modules.developer.DeveloperCommand

class MockCommand : DeveloperCommand() {
  override val name = "Mock"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(name = "target user") targetUser: User?,
    @Argument(name = "command", consumeRest = true) command: String?
  ) {
    if (targetUser == null || command == null) return
    if (context.isFromGuild && context.guild!!.selfMember.hasPermission(
        context.channel as GuildChannel,
        Permission.MESSAGE_MANAGE
      )
    ) {
      when (context) {
        is MessageCommandContext -> context.event.message.delete()
        is SlashCommandContext -> context.event.hook.deleteOriginal()
        else -> throw IllegalStateException("Unknown command context type ${context::class.java.name}")
      }.queue()
    }

    val result =
      context.reply("Executing `$command` on behalf of `${targetUser.asTag}`").await()
    val sentMessage = when (context) {
      is MessageCommandContext -> result as Message
      is SlashCommandContext -> (result as InteractionHook).retrieveOriginal().await()
      else -> throw IllegalStateException("Unknown command context type ${context::class.java.name}")
    }

    val targetMember = try {
      context.guild?.retrieveMember(targetUser)?.await()
    } catch (e: Throwable) {
      null
    }

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
      listOf(),
      listOf(),
      Message.MessageFlag.toBitField(sentMessage.flags),
      null
    )
    val fakeEvent = MessageReceivedEvent(context.jda, context.event.responseNumber, fakeMessage)
    context.jda.eventManager.handle(fakeEvent)
  }
}
