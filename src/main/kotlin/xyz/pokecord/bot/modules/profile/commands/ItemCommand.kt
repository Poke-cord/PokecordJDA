package xyz.pokecord.bot.modules.profile.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.core.structures.pokemon.ItemData
import xyz.pokecord.bot.core.structures.pokemon.items.ItemFactory
import xyz.pokecord.bot.modules.profile.commands.item.GiveItemCommand
import xyz.pokecord.bot.modules.profile.commands.item.TakeItemCommand

class ItemCommand : ParentCommand() {
  override val childCommands = mutableListOf(GiveItemCommand, TakeItemCommand)
  override val name = "Item"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(name = "item", consumeRest = true) itemname: String?
  ) {
    if (!context.hasStarted(true)) return

    if (itemname == null) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.profile.commands.item.errors.noItemName")
        ).build()
      ).queue()
      return
    }

    val itemData = ItemData.getByName(itemname)
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
      itemname.startsWith(
        itemData.name,
        true
      ) -> itemData.name.length
      itemname.startsWith(
        itemData.identifier,
        true
      ) -> itemData.identifier.length
      itemname
        .replace(' ', '-')
        .startsWith(itemData.identifier) -> itemData.identifier.length
      else -> throw IllegalStateException("What the hell did the user supply?? $itemname")
    }

    val args = itemname.drop(nameArgLength).trim().split(" ")
    val (consumed, response) = item.use(context, args)
    if (consumed) {
      module.bot.database.userRepository.consumeInventoryItem(inventoryItem)
    }
    context.reply(response.build()).queue()
  }
}
