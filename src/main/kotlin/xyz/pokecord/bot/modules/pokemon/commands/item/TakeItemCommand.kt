package xyz.pokecord.bot.modules.pokemon.commands.item

import xyz.pokecord.bot.core.structures.discord.MessageCommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.ItemData

object TakeItemCommand : Command() {
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