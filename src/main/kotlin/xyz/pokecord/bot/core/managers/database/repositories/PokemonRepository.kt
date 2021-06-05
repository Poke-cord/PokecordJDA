package xyz.pokecord.bot.core.managers.database.repositories

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Indexes
import com.mongodb.client.result.InsertOneResult
import com.mongodb.reactivestreams.client.ClientSession
import kotlinx.serialization.json.*
import org.bson.BsonDocument
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.aggregate
import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.managers.database.models.User
import xyz.pokecord.bot.core.structures.pokemon.EvolutionChain
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.core.structures.pokemon.Type
import xyz.pokecord.bot.utils.CountResult

class PokemonRepository(
  database: Database,
  private val collection: CoroutineCollection<OwnedPokemon>
) : Repository(database) {
  private val releasedPokemonCollection: CoroutineCollection<OwnedPokemon> =
    database.database.getCollection("releasedPokemon")

  override suspend fun createIndexes() {
    collection.createIndex(Indexes.ascending("ownerId"))
    collection.createIndex(Indexes.compoundIndex(Indexes.ascending("index"), Indexes.ascending("ownerId")))
    collection.createIndex(Indexes.compoundIndex(Indexes.ascending("ownerId"), Indexes.ascending("timestamp")))
  }

  suspend fun getPokemonById(id: Id<OwnedPokemon>): OwnedPokemon? {
    return collection.findOneById(id)
  }

  suspend fun getPokemonByIndex(ownerId: String, index: Int): OwnedPokemon? {
    return collection.findOne(OwnedPokemon::ownerId eq ownerId, OwnedPokemon::index eq index)
  }

  suspend fun getLatestPokemon(ownerId: String): OwnedPokemon? {
    return collection.find(OwnedPokemon::ownerId eq ownerId).sort(descending(OwnedPokemon::timestamp)).limit(1).first()
  }

  suspend fun getUnclaimedPokemonCount(ownerId: String) =
    collection.countDocuments(and(OwnedPokemon::ownerId eq ownerId, OwnedPokemon::rewardClaimed eq false))

  suspend fun claimUnclaimedPokemon(ownerId: String, session: ClientSession? = null): CatchRewardClaimResult {
    val unclaimedMythical = when (session) {
      null -> collection.updateMany(
        and(
          OwnedPokemon::ownerId eq ownerId,
          OwnedPokemon::rewardClaimed eq false,
          OwnedPokemon::id `in` Pokemon.mythicals
        ),
        set(OwnedPokemon::rewardClaimed setTo true)
      )
      else -> collection.updateMany(
        session,
        and(
          OwnedPokemon::ownerId eq ownerId,
          OwnedPokemon::rewardClaimed eq false,
          OwnedPokemon::id `in` Pokemon.mythicals
        ),
        set(OwnedPokemon::rewardClaimed setTo true)
      )
    }

    val unclaimedLegendary = when (session) {
      null -> collection.updateMany(
        and(
          OwnedPokemon::ownerId eq ownerId,
          OwnedPokemon::rewardClaimed eq false,
          OwnedPokemon::id `in` Pokemon.legendaries
        ),
        set(OwnedPokemon::rewardClaimed setTo true)
      )
      else -> collection.updateMany(
        session,
        and(
          OwnedPokemon::ownerId eq ownerId,
          OwnedPokemon::rewardClaimed eq false,
          OwnedPokemon::id `in` Pokemon.legendaries
        ),
        set(OwnedPokemon::rewardClaimed setTo true)
      )
    }

    val unclaimedUltraBeast = when (session) {
      null -> collection.updateMany(
        and(
          OwnedPokemon::ownerId eq ownerId,
          OwnedPokemon::rewardClaimed eq false,
          OwnedPokemon::id `in` Pokemon.mythicals
        ),
        set(OwnedPokemon::rewardClaimed setTo true)
      )
      else -> collection.updateMany(
        session,
        and(
          OwnedPokemon::ownerId eq ownerId,
          OwnedPokemon::rewardClaimed eq false,
          OwnedPokemon::id `in` Pokemon.mythicals
        ),
        set(OwnedPokemon::rewardClaimed setTo true)
      )
    }

    val unclaimedPseudoLegendary = when (session) {
      null -> collection.updateMany(
        and(
          OwnedPokemon::ownerId eq ownerId,
          OwnedPokemon::rewardClaimed eq false,
          OwnedPokemon::id `in` Pokemon.mythicals
        ),
        set(OwnedPokemon::rewardClaimed setTo true)
      )
      else -> collection.updateMany(
        session,
        and(
          OwnedPokemon::ownerId eq ownerId,
          OwnedPokemon::rewardClaimed eq false,
          OwnedPokemon::id `in` Pokemon.mythicals
        ),
        set(OwnedPokemon::rewardClaimed setTo true)
      )
    }

    val unclaimedOthers = when (session) {
      null -> collection.updateMany(
        and(
          OwnedPokemon::ownerId eq ownerId,
          OwnedPokemon::rewardClaimed eq false
        ),
        set(OwnedPokemon::rewardClaimed setTo true)
      )
      else -> collection.updateMany(
        session,
        and(
          OwnedPokemon::ownerId eq ownerId,
          OwnedPokemon::rewardClaimed eq false
        ),
        set(OwnedPokemon::rewardClaimed setTo true)
      )
    }

    return CatchRewardClaimResult(
      unclaimedMythical.modifiedCount.toInt(),
      unclaimedLegendary.modifiedCount.toInt(),
      unclaimedUltraBeast.modifiedCount.toInt(),
      unclaimedPseudoLegendary.modifiedCount.toInt(),
      unclaimedOthers.modifiedCount.toInt()
    )
  }

  data class CatchRewardClaimResult(
    val mythicalCount: Int,
    val legendaryCount: Int,
    val ultraBeastCount: Int,
    val pseudoLegendaryCount: Int,
    val otherCount: Int
  )

  suspend fun getPokemonList(
    ownerId: String,
    limit: Int? = 15,
    skip: Int? = 0,
    searchOptions: PokemonSearchOptions = PokemonSearchOptions(),
    aggregation: ArrayList<Bson> = arrayListOf()
  ): List<OwnedPokemon> {
    if (skip != null) aggregation.add(skip(skip))
    if (limit != null) aggregation.add(limit(limit))
    val result = collection.aggregate<OwnedPokemon>(
      match(OwnedPokemon::ownerId eq ownerId),
      *searchOptions.pipeline,
      *aggregation.toTypedArray(),
    )
    return result.toList()
  }

  suspend fun getPokemonCount(
    ownerId: String,
    searchOptions: PokemonSearchOptions = PokemonSearchOptions(),
    aggregation: ArrayList<Bson> = arrayListOf()
  ): Int {
    val result = collection.aggregate<CountResult>(
      match(OwnedPokemon::ownerId eq ownerId),
      *PokemonSearchOptions(
        null,
        searchOptions.favorites,
        searchOptions.nature,
        searchOptions.rarity,
        searchOptions.shiny,
        searchOptions.type,
        searchOptions.regex,
        searchOptions.searchQuery,
        true
      ).pipeline,
      *aggregation.toTypedArray(),
      Aggregates.count("count")
    ).toList()
    if (result.isEmpty()) return 0
    return result.first().count
  }

  suspend fun insertPokemon(pokemon: OwnedPokemon, session: ClientSession? = null): InsertOneResult {
    return (if (session != null) collection.insertOne(session, pokemon)
    else collection.insertOne(pokemon))
  }

  suspend fun toggleFavoriteStatus(pokemon: OwnedPokemon): OwnedPokemon {
    pokemon.favorite = !pokemon.favorite
    collection.updateOne(OwnedPokemon::_id eq pokemon._id, set(OwnedPokemon::favorite setTo pokemon.favorite))
    return pokemon
  }

  suspend fun setNickname(pokemon: OwnedPokemon, nickname: String?): OwnedPokemon {
    pokemon.nickname = nickname
    collection.updateOne(OwnedPokemon::_id eq pokemon._id, set(OwnedPokemon::nickname setTo pokemon.nickname))
    return pokemon
  }

  suspend fun releasePokemon(pokemon: OwnedPokemon, session: ClientSession) {
    releasedPokemonCollection.insertOne(session, pokemon)
    collection.deleteOne(session, OwnedPokemon::_id eq pokemon._id)
  }

  suspend fun giveXp(pokemon: OwnedPokemon, xp: Int, session: ClientSession? = null) {
    if (session == null) collection.updateOne(OwnedPokemon::_id eq pokemon._id, inc(OwnedPokemon::xp, xp))
    else collection.updateOne(session, OwnedPokemon::_id eq pokemon._id, inc(OwnedPokemon::xp, xp))
  }

  suspend fun levelUpAndEvolveIfPossible(
    pokemon: OwnedPokemon,
    usedItemId: Int? = null,
    gainedXp: Int? = null
  ): Pair<Boolean, Boolean> {
    var leveledUp = false
    var evolved = false

    if (gainedXp != null) {
      pokemon.xp += gainedXp
    }

    var requiredXpToLevelUp = pokemon.requiredXpToLevelUp()
    while (pokemon.level < 100 && requiredXpToLevelUp <= pokemon.xp) {
      if (!leveledUp) leveledUp = true
      pokemon.xp -= requiredXpToLevelUp
      pokemon.level++
      requiredXpToLevelUp = pokemon.requiredXpToLevelUp()
    }

    if (pokemon.id != 790 && pokemon.heldItemId != 206) {
      val evolution = pokemon.data.nextEvolutions.map { EvolutionChain.details(it) }.find { evolutionDetails ->
        val isGenderOk =
          if (evolutionDetails?.genderId != 0) if (evolutionDetails?.genderId == 1) pokemon.gender == 0 else if (evolutionDetails?.genderId == 2) pokemon.gender == 1 else true else true
        val isHeldItemOk =
          if (evolutionDetails?.heldItemId != 0) evolutionDetails?.heldItemId == pokemon.heldItemId else true
        val isKnownMoveOk =
          if (evolutionDetails?.knownMoveId != 0) pokemon.moves.contains(evolutionDetails?.knownMoveId) else true
        val isLevelUpOk = if (evolutionDetails?.evolutionTriggerId == 1) leveledUp else true
        val isMinimumLevelOk = (evolutionDetails?.minimumLevel ?: 0) <= pokemon.level
        val isTradeStateOk = evolutionDetails?.evolutionTriggerId != 2 // TODO: change after implementing trade
        val isTriggerItemOk =
          if (evolutionDetails?.evolutionTriggerId == 3) evolutionDetails.triggerItemId == usedItemId else true

        isGenderOk &&
            isHeldItemOk &&
            isKnownMoveOk &&
            isLevelUpOk &&
            isMinimumLevelOk &&
            isTradeStateOk &&
            isTriggerItemOk &&
            evolutionDetails?.knownMoveTypeId == 0 &&
            evolutionDetails.locationId == 0 &&
            evolutionDetails.partySpeciesId == 0 &&
            evolutionDetails.partyTypeId == 0 &&
            evolutionDetails.relativePhysicalStats == 0 &&
            evolutionDetails.timeOfDay == "" &&
            evolutionDetails.minimumAffection == 0 &&
            evolutionDetails.minimumBeauty == 0 &&
            evolutionDetails.minimumHappiness == 0
      }

      if (evolution != null) {
        pokemon.id = evolution.evolvedSpeciesId
        evolved = true
      }
    }

    val updatesBson = mutableListOf<Bson>()
    if (leveledUp || gainedXp != null) {
      updatesBson.add(
        set(
          OwnedPokemon::level setTo pokemon.level,
          OwnedPokemon::xp setTo pokemon.xp
        )
      )
    }
    if (evolved) {
      updatesBson.add(set(OwnedPokemon::id setTo pokemon.id))
    }

    if (leveledUp || evolved || gainedXp != null) {
      collection.updateOne(
        OwnedPokemon::_id eq pokemon._id,
        combine(updatesBson)
      )
    }

    return Pair(leveledUp, evolved)
  }

  suspend fun giftPokemon(sender: User, receiver: User, pokemon: OwnedPokemon, session: ClientSession) {
    if (pokemon.trainerId == null) {
      collection.updateMany(
        session,
        OwnedPokemon::_id eq pokemon._id,
        set(
          OwnedPokemon::trainerId setTo sender.id
        )
      )
    }
    collection.updateOne(
      session,
      OwnedPokemon::_id eq pokemon._id,
      set(
        OwnedPokemon::index setTo receiver.nextPokemonIndices.first(),
        OwnedPokemon::ownerId setTo receiver.id
      )
    )
  }

  suspend fun giveItem(_id: Id<OwnedPokemon>, itemId: Int, extraOps: suspend (session: ClientSession) -> Unit = {}) {
    val session = database.startSession()
    session.use {
      it.startTransaction()
      collection.updateOne(it, OwnedPokemon::_id eq _id, set(OwnedPokemon::heldItemId setTo itemId))
      extraOps(it)
      it.commitTransactionAndAwait()
    }
  }

  suspend fun takeItem(_id: Id<OwnedPokemon>, extraOps: suspend (session: ClientSession) -> Unit = {}) {
    val session = database.startSession()
    session.use {
      it.startTransaction()
      collection.updateOne(it, OwnedPokemon::_id eq _id, set(OwnedPokemon::heldItemId setTo 0))
      extraOps(it)
      it.commitTransactionAndAwait()
    }
  }

  suspend fun teachMove(pokemon: OwnedPokemon, slot: Int, moveId: Int) {
    if (pokemon.moves.size < 4) pokemon.moves = mutableListOf(0, 0, 0, 0)
    pokemon.moves[slot - 1] = moveId
    collection.updateOne(OwnedPokemon::_id eq pokemon._id, set(OwnedPokemon::moves setTo pokemon.moves))
  }

  data class PokemonSearchOptions(
    val order: String? = null,
    val favorites: Boolean? = null,
    val nature: String? = null,
    val rarity: String? = null,
    val shiny: Boolean? = null,
    val type: String? = null,
    val regex: Regex? = null,
    val searchQuery: String? = null,
    val noSorting: Boolean = false
  ) {
    private var ids: Set<Int> = setOf()
    private var searchIds: Set<Int> = setOf()
    private var orderBson: Bson? = null

    init {
      val rarities: MutableList<String> =
        (rarity?.toLowerCase()?.split(Regex(",( )?"))?.toMutableList() ?: mutableListOf())

      for (rarity in rarities) {
        when {
          arrayOf("l", "lg", "leg", "legendary").contains(rarity) -> {
            ids = ids + Pokemon.legendaries
          }
          arrayOf("m", "myth", "mythic", "mythical").contains(rarity) -> {
            ids = ids + Pokemon.mythicals
          }
          arrayOf("s", "starter").contains(rarity) -> {
            ids = ids + Pokemon.starters
          }
          arrayOf("u", "ub", "ultrabeast", "ultra-beast").contains(rarity) -> {
            ids = ids + Pokemon.ultraBeasts
          }
          arrayOf("p", "ps", "pseudo", "pseudolegendary", "pseudo-legendary").contains(rarity) -> {
            ids = ids + Pokemon.pseudoLegendaries
          }
        }
      }

      val allIds = (0 until Pokemon.maxId).toSet()

      if (type != null) {
        val types = type.split(",").mapNotNull { Type.getByName(it) }
        ids = (ids.ifEmpty { allIds }).intersect(Pokemon.getByTypes(types).map { it.id })
      }

      (if (regex != null) Pokemon.searchRegex(regex) else if (searchQuery != null) Pokemon.search(searchQuery) else null)
        ?.map { it.id }?.let {
          searchIds = it.toSet()
        }

      if (order != null) {
        val lowerCaseOrder = order.toLowerCase()
        orderBson = when {
          arrayOf("i", "iv").contains(lowerCaseOrder) -> {
            sort(descending(OwnedPokemon::totalIv))
          }
          arrayOf("l", "lv", "level").contains(lowerCaseOrder) -> {
            sort(descending(OwnedPokemon::level))
          }
          arrayOf("d", "dex", "pokedex").contains(lowerCaseOrder) -> {
            sort(ascending(OwnedPokemon::id))
          }
          arrayOf("t", "time").contains(lowerCaseOrder) -> {
            sort(ascending(OwnedPokemon::timestamp))
          }
          else -> null
        }
      }
    }

    val pipeline: Array<Bson>
      get() {
        val aggregation = arrayListOf<Bson>()
        if (favorites == true) {
          aggregation.add(match(OwnedPokemon::favorite eq true))
        }
        if (shiny == true) {
          aggregation.add(match(OwnedPokemon::shiny eq true))
        }
        if (nature != null) {
          aggregation.add(match(OwnedPokemon::nature eq nature))
        }
        if (ids.isNotEmpty()) {
          aggregation.add(match(OwnedPokemon::id `in` ids.asIterable()))
        }
        if (regex != null || searchQuery != null) {
          aggregation.add(
            BsonDocument.parse(
              buildJsonObject {
                putJsonObject("\$match") {
                  putJsonArray("\$or") {
                    addJsonObject {
                      putJsonObject("nickname") {
                        put("\$regex", (regex ?: searchQuery!!).toString())
                        put(
                          "\$options",
                          if (regex?.options?.contains(RegexOption.IGNORE_CASE) == true || regex == null) "i" else ""
                        )
                      }
                    }
                    addJsonObject {
                      putJsonObject("id") {
                        putJsonArray("\$in") {
                          searchIds.forEach {
                            add(it)
                          }
                        }
                      }
                    }
                  }
                }
              }.toString()
            )
          )
        }
        if (!noSorting) aggregation.add(orderBson ?: sort(ascending(OwnedPokemon::index)))
        return aggregation.toTypedArray()
      }
  }
}
