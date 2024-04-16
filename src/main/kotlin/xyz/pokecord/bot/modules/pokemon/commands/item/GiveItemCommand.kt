package xyz.pokecord.bot.modules.pokemon.commands.item

import xyz.pokecord.bot.core.structures.discord.MessageCommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.ItemData
import xyz.pokecord.bot.core.structures.pokemon.items.ItemFactory

object GiveItemCommand : Command() {
  override val name = "give"

  override var aliases = arrayOf("g", "hold")

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

    val item = ItemFactory.items[itemData.id]!!
    if (!item.canBeHeld) {
      context.reply(
        context.embedTemplates.error(
          context.translate("modules.profile.commands.item.give.errors.canNotBeGiven")
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