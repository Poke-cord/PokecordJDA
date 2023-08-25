package xyz.pokecord.bot.core.structures.pokemon.items

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.utils.PokemonStats
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.round
import kotlin.random.Random

class RedeemItem(
  id: Int,
  private val disallowedPokemonIds: List<Int> = listOf(),
  private val minIvPercentage: Int = 0,
  private val maxIvPercentage: Int = 100
) : Item(id, false) {

  override suspend fun use(context: ICommandContext, args: List<String>): UsageResult {
    val pokemonName = args.joinToString(" ")
    if (pokemonName.isEmpty()) {
      return UsageResult(
        false,
        context.embedTemplates.error(context.translate("items.redeem.errors.noPokemonName"))
      )
    }
    val pokemon = Pokemon.getByName(pokemonName)
      ?: return UsageResult(
        false,
        context.embedTemplates.error(context.translate("misc.errors.pokemonNotFound"))
      )
    if (disallowedPokemonIds.contains(pokemon.id)) {
      return UsageResult(
        false,
        context.embedTemplates.error(
          context.translate(
            "items.redeem.errors.disallowedPokemon",
            mapOf(
              "redeem" to data.name,
              "pokemon" to context.translator.pokemonName(pokemon)!!
            )
          )
        )
      )
    }

    val ownedPokemon = context.bot.database.userRepository.givePokemon(
      context.getUserData(),
      pokemon.id,
      false,
      ivs = getIvs()
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
      ).setFooter(context.translate("misc.embeds.pokemonObtained.footer"))
    )
  }

  private fun getIvs(): PokemonStats {
    if (id == Redeems.Chromatic.id) return OwnedPokemon.defaultIV()
    val minTotal = round((minIvPercentage / 100.0) * 186).toInt()
    val maxTotal = round((maxIvPercentage / 100.0) * 186).toInt()
    val items = spreadRandomly(0, 31, 6, Random.nextInt(minTotal, maxTotal + 1))
    items.shuffle() // People say attack IV is always very close to 0, so we shuffle them so that it's not only the attack stat that's affected.
    return PokemonStats(
      items[0],
      items[1],
      items[2],
      items[3],
      items[4],
      items[5]
    )
  }

  enum class Redeems(
    val id: Int,
    val identifier: String,
    val itemName: String,
    val cost: Int,
    val flingPower: Int = 0,
    val flingEffectId: Int = 0,
    val useGems: Boolean = true
  ) {
    Common(10000000, "common-redeem", "Common Redeem", 100),
    Rare(10000001, "rare-redeem", "Rare Redeem", 125),
    Epic(10000002, "epic-redeem", "Epic Redeem", 175),
    Legendary(10000003, "legendary-redeem", "Legendary Redeem", 200),
    Fabled(10000004, "fabled-redeem", "Fabled Redeem", 250),
    Chromatic(10000005, "chromatic-redeem", "Chromatic Redeem", 150),
  }

  companion object {
    private fun RedeemItem.asPair(): Pair<Int, RedeemItem> {
      return id to this
    }

    // From StackOverflow. Ported from original JS version
    fun spreadRandomly(
      min: Int,
      max: Int,
      length: Int,
      sum: Int
    ): Array<Int> {
      var total = sum
      return Array(length) { i ->
        val sMin = (length - i - 1) * min
        val sMax = (length - i - 1) * max
        val offset = max(total - sMax, min)
        val random = 1 + arrayOf(total - offset, max - offset, total - sMin - min).minOf { it }
        val value = floor(Random.nextDouble() * random + offset).toInt()
        total -= value
        value
      }
    }

    val redeemMap: MutableMap<Int, Item> = mutableMapOf(
      RedeemItem(
        Redeems.Common.id,
        Pokemon.legendaries + Pokemon.mythicals,
        55,
        80
      ).asPair(),
      Redeems.Rare.id to RedeemItem(
        Redeems.Rare.id,
        Pokemon.legendaries + Pokemon.mythicals,
        55,
        90
      ),
      RedeemItem(
        Redeems.Epic.id,
        minIvPercentage = 50,
        maxIvPercentage = 70
      ).asPair(),
      RedeemItem(
        Redeems.Chromatic.id
      ).asPair(),
      RedeemItem(
        Redeems.Legendary.id,
        minIvPercentage = 55,
        maxIvPercentage = 75
      ).asPair(),
      RedeemItem(
        Redeems.Fabled.id,
        minIvPercentage = 55,
        maxIvPercentage = 85
      ).asPair()
    )

    const val categoryId = 1000
  }
}
