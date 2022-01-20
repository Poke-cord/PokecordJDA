package xyz.pokecord.bot.modules.profile.commands.item

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.ItemData
import xyz.pokecord.bot.core.structures.pokemon.items.ItemFactory

object UseItemCommand: Command() {
  override val name = "use"
  override var aliases = arrayOf("u")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(name = "item", consumeRest = true) itemName: String?
  ) {
    if (!context.hasStarted(true)) return

    if (itemName == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.profile.commands.item.errors.noItemName")
        ).build()
      ).queue()
      return
    }

    val itemData = ItemData.getByName(itemName)
    if (itemData == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.profile.commands.item.errors.itemNotFound")
        ).build()
      ).queue()
      return
    }

    val inventoryItem = module.bot.database.userRepository.getInventoryItem(context.author.id, itemData.id)

    if (inventoryItem == null || inventoryItem.amount < 1) {
      context.reply(
        context.embedTemplates.error(
          context.translate(
            "modules.profile.commands.item.errors.itemNotOwned.description",
            mapOf(
              "user" to context.author.asMention,
              "item" to itemData.name
            )
          ),
          context.translate(
            "modules.profile.commands.item.errors.itemNotOwned.title"
          )
        ).build()
      ).queue()
      return
    }

    val item = ItemFactory.items[itemData.id]
    if (item == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.profile.commands.item.errors.notAvailable")
        ).build()
      ).queue()
      return
    }

    val nameArgLength = when {
      itemName.startsWith(
        itemData.name,
        true
      ) -> itemData.name.length
      itemName.startsWith(
        itemData.identifier,
        true
      ) -> itemData.identifier.length
      itemName
        .replace(' ', '-')
        .startsWith(itemData.identifier) -> itemData.identifier.length
      else -> throw IllegalStateException("What the hell did the user supply?? $itemName")
    }

    val args = itemName.drop(nameArgLength).trim().split(" ")
    val (consumed, response) = item.use(context, args)
    if (consumed) {
      module.bot.database.userRepository.consumeInventoryItem(inventoryItem)
    }
    context.reply(response.build()).queue()
  }
}