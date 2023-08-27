package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.pokemon.ItemData
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import kotlin.random.Random

object ChaosRedeemItem : Item(10000006, false) {
  const val categoryId = RedeemItem.categoryId

  override suspend fun use(context: ICommandContext, args: List<String>): UsageResult {
    val pokemon = Pokemon.getById(Random.nextInt(1, Pokemon.maxId + 1))

    val ownedPokemon =
      context.bot.database.userRepository.givePokemon(
        context.getUserData(),
        pokemon!!.id
        )

    return UsageResult(
      true,
      context.embedTemplates.normal(
        context.translate(
          "items.redeem.embed.description",
          mapOf(
            "pokemon" to pokemon.name,
            "level" to ownedPokemon.level.toString(),
            "ivPercentage" to ownedPokemon.ivPercentage,
            "user" to context.author.asMention
          )
        ),
        context.translate(
          "items.redeem.embed.title", "redeem" to data.name
        ),
      )
    )
  }

  val itemData = ItemData(
    id,
    "chaos-redeem",
    "Chaos Redeem",
    categoryId,
    50,
    0,
    0,
    true
  )
}
