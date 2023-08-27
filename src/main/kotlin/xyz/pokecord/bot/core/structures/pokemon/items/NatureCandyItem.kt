package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.api.ICommandContext

object NatureCandyItem : Item(10010002, false) {
  const val categoryId = 1001

  private val natureMintItems by lazy {
    ItemFactory.items.values.filter { it.data.categoryId == NatureMintItem.categoryId }
  }

  override suspend fun use(context: ICommandContext, args: List<String>): UsageResult {
    val userData = context.getUserData()
    val randomNatureMint = natureMintItems.random()
    context.bot.database.userRepository.addInventoryItem(userData.id, randomNatureMint.data.id, 1)
    return UsageResult(
      true,
      context.embedTemplates.normal(
        context.translate("items.natureCandy.embed.description",
          mapOf(
            "natureMint" to randomNatureMint.data.name,
            "user" to context.author.asMention
          )
        ),
        context.translate("items.natureCandy.embed.title")
      )
    )
  }
}
