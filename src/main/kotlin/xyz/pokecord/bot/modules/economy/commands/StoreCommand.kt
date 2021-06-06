package xyz.pokecord.bot.modules.economy.commands

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.Order
import xyz.pokecord.bot.core.structures.discord.MessageCommandContext
import xyz.pokecord.bot.core.structures.discord.SlashCommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.store.packages.Package
import xyz.pokecord.bot.utils.Config
import xyz.pokecord.bot.utils.Confirmation
import xyz.pokecord.bot.utils.api.PayPal
import xyz.pokecord.bot.utils.extensions.awaitReaction

class StoreCommand : Command() {
  override val name = "Store"

  override var aliases = arrayOf("donate", "donation")

  private val payPal by lazy { PayPal(module.bot.database) }

  @Executor
  suspend fun execute(context: ICommandContext) {
    val orderData = module.bot.database.orderRepository.getUnpaidOrder(context.author.id)
    if (orderData != null) {
      val confirmation = Confirmation(context, 60_000)
      val confirmed = confirmation.result(
        context.embedTemplates.confirmation(
          context.translate("modules.economy.commands.store.embed.confirmation.cancelOldOrder")
        )
      )
      when {
        confirmation.timedOut -> {
          confirmation.sentMessage!!.editMessage(
            context.embedTemplates.error(
              context.translate("modules.economy.commands.store.errors.cancelOldOrder.noAction")
            ).build()
          )
        }
        confirmed -> {
          confirmation.sentMessage!!.editMessage(
            context.embedTemplates.error(
              context.translate("modules.economy.commands.store.errors.cancelOldOrder.noAction")
            ).build()
          )
          module.bot.database.orderRepository.deleteOrder(orderData)
          // delete old order
        }
        else -> {
          // TODO: what to do if they decide not to cancel the old order?
        }
      }
      return
    }

    val packagesEmojis = mutableListOf<String>()

    val packagesText = Package.packages.mapIndexed { i, it ->
      val emoji = Config.Emojis.alphabet[i]
      packagesEmojis.add(emoji)
      "$emoji **${context.translate("store.packages.${it.id}.name")}** ~ ${context.translate("store.packages.${it.id}.description")}"
    }.joinToString("\n")

    val result = context.reply(
      context.embedTemplates.normal(
        context.translate("modules.economy.commands.store.embed.description") + "\n\n" + packagesText,
        context.translate("modules.economy.commands.store.embed.title")
      ).build()
    ).await()
    val message = when (context) {
      is MessageCommandContext -> result as Message
      is SlashCommandContext -> (result as InteractionHook).retrieveOriginal().await()
      else -> throw IllegalStateException("Unknown command context type ${context::class.java.name}")
    }

    packagesEmojis.forEach {
      message.addReaction(it).queue()
    }

    val packageReaction = message.awaitReaction(context.author) {
      it.reactionEmote.isEmoji && packagesEmojis.contains(it.reactionEmote.emoji)
    }

    val canClearReactions = context.channel is GuildChannel && context.guild!!.selfMember.hasPermission(
      context.channel as GuildChannel,
      Permission.MESSAGE_MANAGE
    )

    if (canClearReactions) {
      message.clearReactions()
    } else {
      packagesEmojis.forEach {
        message.removeReaction(it).queue()
      }
    }

    val pkg = Package.packages[packagesEmojis.indexOf(packageReaction.reactionEmote.emoji)]

    val itemsEmojis = mutableListOf<String>()

    val itemsText = pkg.items.mapIndexed { i, it ->
      val emoji = Config.Emojis.alphabet[i]
      itemsEmojis.add(emoji)
      "$emoji **${context.translate("store.packages.${pkg.id}.items.${it.id}")}** ~ $${
        context.translator.numberFormat(
          it.price
        )
      }"
    }.joinToString("\n")

    message.editMessage(
      context.embedTemplates.normal(
        context.translate("store.packages.${pkg.id}.embed.description") + "\n\n" + itemsText,
        context.translate("store.packages.${pkg.id}.embed.title")
      ).build()
    ).queue()

    itemsEmojis.forEach {
      message.addReaction(it).queue()
    }


    val itemReaction = message.awaitReaction(context.author) {
      it.reactionEmote.isEmoji && itemsEmojis.contains(it.reactionEmote.emoji)
    }
    if (canClearReactions) {
      message.clearReactions()
    } else {
      itemsEmojis.forEach {
        message.removeReaction(it).queue()
      }
    }
    val item = pkg.items[itemsEmojis.indexOf(itemReaction.reactionEmote.emoji)]
    val itemName = context.translate("store.packages.${pkg.id}.items.${item.id}")

    val orderId = payPal.createOrder(
      context.author.name,
      item.price,
      itemName
    )
    if (orderId == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.economy.commands.store.errors.failedToCancelOrder")
        ).build()
      ).queue()
    } else {
      module.bot.database.orderRepository.createOrder(
        Order(
          orderId,
          pkg.id,
          item.id,
          item.price.toDouble(),
          context.author.id,
          context.author.name
        )
      )

      try {
        val privateChannel = context.author.openPrivateChannel().await()
        privateChannel.sendMessage(
          context.embedTemplates.normal(
            context.translate(
              "modules.economy.commands.store.dm.embed.description",
              mapOf(
                "item" to itemName, "link" to payPal.getCheckoutLink(orderId)
              )
            )
          ).build()
        ).await()
      } catch (t: Throwable) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.economy.commands.store.errors.dmsClosed")
          ).build()
        ).queue()
        payPal.deleteOrder(orderId)
        return
      }

      context.reply(
        context.embedTemplates.normal(
          context.translate("modules.economy.commands.store.embed.orderCreated.description"),
          context.translate("modules.economy.commands.store.embed.orderCreated.title")
        ).build()
      )
    }
  }
}
