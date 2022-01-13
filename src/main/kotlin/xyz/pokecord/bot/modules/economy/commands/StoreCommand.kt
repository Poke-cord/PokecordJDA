package xyz.pokecord.bot.modules.economy.commands

import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.Order
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.store.packages.Package
import xyz.pokecord.bot.utils.ButtonMenu
import xyz.pokecord.bot.utils.Confirmation

class StoreCommand : Command() {
  override val name = "Store"

  override var aliases = arrayOf("donate", "donation")
//  override var enabled = false

  @Executor
  suspend fun execute(context: ICommandContext) {
    val orderData = module.bot.database.orderRepository.getUnpaidOrder(context.author.id)
    if (orderData != null) {
      val confirmation = Confirmation(context, timeout = 60_000)
      val confirmed = confirmation.result(
        context.embedTemplates.confirmation(
          context.translate("modules.economy.commands.store.embed.confirmation.cancelOldOrder")
        )
      )
      if (confirmed) {
        confirmation.sentMessage!!.editMessageEmbeds(
          context.embedTemplates.error(
            context.translate("modules.economy.commands.store.errors.cancelOldOrder.orderCancelled")
          ).build()
        ).queue()
        module.bot.database.orderRepository.deleteOrder(orderData)
      } else {
        val itemName = context.translate("store.packages.${orderData.packageId}.items.${orderData.itemId}")

        confirmation.sentMessage!!.editMessageEmbeds(
          context.embedTemplates.normal(
            context.translate(
              "modules.economy.commands.store.embed.oldOrderDescription",
              mapOf(
                "item" to itemName,
                "link" to module.bot.payPal.getCheckoutLink(orderData.orderId)
              )
            )
          ).build()
        ).queue()
      }
      return
    }

    val packageButtons = mutableListOf<ButtonMenu.ButtonData>()
    val itemButtons = mutableListOf<ButtonMenu.ButtonData>()

    val packagesText = Package.packages.map {
      val packageName = context.translate("store.packages.${it.id}.name")
      packageButtons.add(ButtonMenu.ButtonData(it.id, packageName, null))
      "**${packageName}** ~ ${context.translate("store.packages.${it.id}.description")}"
    }.joinToString("\n")

    val packagesEmbed = context.embedTemplates.normal(
      context.translate("modules.economy.commands.store.embed.description") + "\n\n" + packagesText,
      context.translate("modules.economy.commands.store.embed.title")
    ).build()

    val buttonMenu = object : ButtonMenu(
      context,
      packageButtons,
      packagesEmbed
    ) {
      private var pkg: Package? = null

      override suspend fun onButtonClick(button: ButtonData, context: ICommandContext, event: ButtonClickEvent) {
        event.deferReply()

        if (packageButtons.contains(button)) {
          pkg = Package.packages.find { it.id == button.id }!!

          val itemsText = pkg!!.items.map {
            val itemName = context.translate("store.packages.${pkg!!.id}.items.${it.id}")
            itemButtons.add(ButtonData(it.id, itemName, null))
            "**${itemName}** ~ $${
              context.translator.numberFormat(
                it.price
              )
            }"
          }.joinToString("\n")

          embed = context.embedTemplates.normal(
            context.translate("store.packages.${pkg!!.id}.embed.description") + "\n\n" + itemsText,
            context.translate("store.packages.${pkg!!.id}.embed.title")
          ).build()
          buttons = itemButtons
          start()
        } else if (itemButtons.contains(button)) {
          val item = pkg!!.items.find { it.id == button.id }!!
          val itemName = context.translate("store.packages.${pkg!!.id}.items.${item.id}")
          val orderId = module.bot.payPal.createOrder(
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
                pkg!!.id,
                item.id,
                item.price.toDouble(),
                context.author.id,
                context.author.asTag
              )
            )

            try {
              val privateChannel = context.author.openPrivateChannel().await()
              privateChannel.sendMessageEmbeds(
                context.embedTemplates.normal(
                  context.translate(
                    "modules.economy.commands.store.dm.embed.description",
                    mapOf(
                      "item" to itemName, "link" to module.bot.payPal.getCheckoutLink(orderId)
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
              module.bot.payPal.deleteOrder(orderId)
              return
            }

            sentMessage?.editMessageEmbeds(
              context.embedTemplates.normal(
                context.translate("modules.economy.commands.store.embed.orderCreated.description"),
                context.translate("modules.economy.commands.store.embed.orderCreated.title")
              ).build()
            )?.queue()
          }
        }
      }
    }

    buttonMenu.start()
  }
}
