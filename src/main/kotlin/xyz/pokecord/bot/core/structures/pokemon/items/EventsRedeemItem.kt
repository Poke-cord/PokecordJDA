package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.pokemon.ItemData
import xyz.pokecord.bot.core.structures.pokemon.SpecialEvents
import kotlin.random.Random

object EventsRedeemItem : Item(10000007, false) {
  const val categoryId = RedeemItem.categoryId

  override suspend fun use(context: ICommandContext, args: List<String>): UsageResult {
    val events = SpecialEvents.getCurrentCatchableEvents()
    val eventPokemon = events.flatMap { it.catchableCustomPokemon.values }.flatten()

    val randomEventPokemon = eventPokemon.randomOrNull()
      ?: return UsageResult(
        false,
        context.embedTemplates.error(
          context.translate("items.redeem.errors.noCurrentEvents")
        )
      )

    val shiny = Random.nextInt(100) < 2
    val ownedPokemon =
      context.bot.database.userRepository.givePokemon(context.getUserData(), randomEventPokemon.id, shiny = shiny)

    return UsageResult(
      true,
      context.embedTemplates.normal(
        context.translate(
          "items.redeem.embed.description",
          mapOf(
            "pokemon" to randomEventPokemon.name,
            "level" to ownedPokemon.level.toString(),
            "ivPercentage" to ownedPokemon.ivPercentage,
            "user" to context.author.asMention
          )
        ),
        context.translate(
          "items.redeem.embed.title", "redeem" to data.name
        ),
      ).setFooter(context.translate("misc.embeds.pokemonObtained.footer"))
    )
  }

  val itemData = ItemData(
    id,
    "events-redeem",
    "Events Redeem",
    categoryId,
    250,
    0,
    0,
    true
  )
}
