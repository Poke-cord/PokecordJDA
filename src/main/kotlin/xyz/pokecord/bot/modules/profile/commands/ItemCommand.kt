package xyz.pokecord.bot.modules.profile.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.MessageCommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.discord.base.ParentCommand
import xyz.pokecord.bot.core.structures.pokemon.ItemData
import xyz.pokecord.bot.core.structures.pokemon.items.ItemFactory
import xyz.pokecord.bot.core.structures.pokemon.items.RedeemItem

class ItemCommand : ParentCommand() {
  override val name = "Item"

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

  @ChildCommand
  class GiveItemCommand : Command() {
    override val name = "give"

    override var aliases = arrayOf("g")

    @Executor
    suspend fun execute(
      context: MessageCommandContext,
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

      if (itemData.categoryId == RedeemItem.categoryId) {
        context.reply(
          context.embedTemplates.error(
            context.translate("modules.profile.commands.item.give.errors.redeem")
          ).build()
        ).queue()
        return
      }

      val userData = context.getUserData()
      val selectedPokemon = module.bot.database.pokemonRepository.getPokemonById(userData.selected!!)
      if (selectedPokemon == null) {
        context.reply(context.embedTemplates.error(context.translate("misc.errors.pokemonNotFound")).build()).queue()
        return
      }

      if (selectedPokemon.heldItemId != 0) {
        context.reply(
          context.embedTemplates.error(
            context.translate(
              "modules.profile.commands.item.give.errors.alreadyHolding",
              "pokemon" to context.translator.pokemonDisplayName(selectedPokemon)
            )
          )
            .build()
        ).queue()
        return
      }

      module.bot.database.pokemonRepository.giveItem(userData.selected!!, itemData.id) {
        module.bot.database.userRepository.consumeInventoryItem(inventoryItem, 1, it)
      }

      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.profile.commands.item.give.embed.description",
            mapOf(
              "item" to itemData.name,
              "pokemon" to context.translator.pokemonDisplayName(selectedPokemon),
              "user" to context.author.asMention
            )
          ),
          context.translate("modules.profile.commands.item.give.embed.title")
        ).build()
      ).queue()
    }
  }

  @ChildCommand
  class TakeItemCommand : Command() {
    override val name = "Take"

    override var aliases = arrayOf("t")

    @Executor
    suspend fun execute(
      context: MessageCommandContext
    ) {
      if (!context.hasStarted(true)) return

      val userData = context.getUserData()
      val selectedPokemon = module.bot.database.pokemonRepository.getPokemonById(userData.selected!!)

      if (selectedPokemon == null) {
        context.reply(context.embedTemplates.error(context.translate("misc.errors.pokemonNotFound")).build()).queue()
        return
      }

      if (selectedPokemon.heldItemId == 0) {
        context.reply(
          context.embedTemplates.error(
            context.translate(
              "modules.profile.commands.item.take.errors.notHolding",
              "pokemon" to context.translator.pokemonDisplayName(selectedPokemon)
            )
          )
            .build()
        ).queue()
        return
      }

      val itemData = ItemData.getById(selectedPokemon.heldItemId)
      if (itemData == null) {
        context.reply(
          context.embedTemplates.error(
            context.translate(
              "modules.profile.commands.item.take.errors.holdingUnknownItem",
              "pokemon" to context.translator.pokemonDisplayName(selectedPokemon)
            )
          ).build()
        )
        return
      }

      module.bot.database.pokemonRepository.takeItem(selectedPokemon._id) {
        module.bot.database.userRepository.addInventoryItem(context.author.id, itemData.id, session = it)
      }

      context.reply(
        context.embedTemplates.normal(
          context.translate(
            "modules.profile.commands.item.take.embed.description",
            mapOf(
              "item" to itemData.name,
              "pokemon" to context.translator.pokemonDisplayName(selectedPokemon),
              "user" to context.author.asMention
            )
          ),
          context.translate("modules.profile.commands.item.take.embed.title")
        ).build()
      ).queue()
    }
  }
}
