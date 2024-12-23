package xyz.pokecord.bot.core.managers.database.repositories

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.ClientSession
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.aggregate
import org.litote.kmongo.coroutine.commitTransactionAndAwait
import org.litote.kmongo.id.toId
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.Cache
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.managers.database.models.TransferLog
import xyz.pokecord.bot.core.managers.database.models.User
import xyz.pokecord.bot.core.structures.pokemon.*
import xyz.pokecord.bot.core.structures.pokemon.items.VoidStoneItem
import xyz.pokecord.bot.utils.*
import xyz.pokecord.utils.withCoroutineLock
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class PokemonRepository(
  database: Database,
  private val cache: Cache,
  private val collection: CoroutineCollection<OwnedPokemon>
) : Repository(database) {
  private val releasedPokemonCollection: CoroutineCollection<OwnedPokemon> =
    database.database.getCollection("releasedPokemon")

  override suspend fun createIndexes() {
    collection.createIndex(Indexes.ascending("level"))
    collection.createIndex(Indexes.ascending("totalIv"))
    collection.createIndex(Indexes.ascending("ownerId"))
    collection.createIndex(Indexes.compoundIndex(Indexes.ascending("ownerId"), Indexes.ascending("id")))
    collection.createIndex(Indexes.compoundIndex(Indexes.ascending("ownerId"), Indexes.ascending("nickname")))
    collection.createIndex(Indexes.compoundIndex(Indexes.ascending("ownerId"), Indexes.ascending("index")))
    collection.createIndex(Indexes.compoundIndex(Indexes.ascending("ownerId"), Indexes.descending("timestamp")))
  }

  suspend fun getPokemonByIds(ids: List<Id<OwnedPokemon>>): List<OwnedPokemon> {
    return collection.find(OwnedPokemon::_id `in` ids).toList()
  }

  suspend fun getPokemonById(id: Id<OwnedPokemon>): OwnedPokemon? {
    return collection.findOneById(id)
  }

  suspend fun getPokemonByIndex(ownerId: String, index: Int): OwnedPokemon? {
    return collection.findOne(OwnedPokemon::ownerId eq ownerId, OwnedPokemon::index eq index)
  }

  suspend fun getPokemonByTotalIv(ownerId: String, totalIv: Int): OwnedPokemon? {
    return collection.findOne(OwnedPokemon::ownerId eq ownerId, OwnedPokemon::totalIv eq totalIv)
  }

  suspend fun getLatestPokemon(ownerId: String): OwnedPokemon? {
    return collection.find(OwnedPokemon::ownerId eq ownerId).sort(descending(OwnedPokemon::timestamp)).limit(1).first()
  }

  suspend fun getUnclaimedPokemonCount(ownerId: String) =
    collection.countDocuments(and(OwnedPokemon::ownerId eq ownerId, OwnedPokemon::rewardClaimed eq false))

  suspend fun getSpecificUnclaimedPokemonCount(ownerId: String): SpecificPokemonCounts {
    val mythicCount = collection.countDocuments(
      and(
        OwnedPokemon::ownerId eq ownerId,
        OwnedPokemon::rewardClaimed eq false,
        OwnedPokemon::id `in` Pokemon.mythicals
      )
    )

    val legendaryCount = collection.countDocuments(
      and(
        OwnedPokemon::ownerId eq ownerId,
        OwnedPokemon::rewardClaimed eq false,
        OwnedPokemon::id `in` Pokemon.legendaries
      )
    )

    val ubCount = collection.countDocuments(
      and(
        OwnedPokemon::ownerId eq ownerId,
        OwnedPokemon::rewardClaimed eq false,
        OwnedPokemon::id `in` Pokemon.ultraBeasts
      )
    )

    val psLegendaryCount = collection.countDocuments(
      and(
        OwnedPokemon::ownerId eq ownerId,
        OwnedPokemon::rewardClaimed eq false,
        OwnedPokemon::id `in` Pokemon.pseudoLegendaries
      )
    )

    return SpecificPokemonCounts(
      mythicCount,
      legendaryCount,
      ubCount,
      psLegendaryCount
    )
  }
  data class SpecificPokemonCounts(
    val mythicCount: Long,
    val leggyCount: Long,
    val ubCount: Long,
    val psLeggyCount: Long,
  )

  suspend fun updateOwnerId(pokemon: OwnedPokemon, newOwnerId: String, clientSession: ClientSession? = null) {
    val oldOwnerId = pokemon.ownerId
    pokemon.favorite = false
    pokemon.ownerId = newOwnerId
    val filter = OwnedPokemon::_id eq pokemon._id
    val update = combine(
      set(
        OwnedPokemon::ownerId setTo newOwnerId,
        OwnedPokemon::favorite setTo false
      ),
      if (pokemon.trainerId == null) set(OwnedPokemon::trainerId setTo oldOwnerId)
      else EMPTY_BSON
    )
    if (clientSession == null) {
      collection.updateOne(filter, update)
    } else {
      collection.updateOne(clientSession, filter, update)
    }
  }

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
  ) {
    val totalCount get() = mythicalCount + legendaryCount + ultraBeastCount + pseudoLegendaryCount + otherCount
  }

  suspend fun getPokemonList(
    ownerId: String,
    limit: Int? = 15,
    skip: Int? = 0,
    searchOptions: PokemonSearchOptions = PokemonSearchOptions(),
    aggregation: MutableList<Bson> = mutableListOf()
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

  suspend fun getPokemonIds(
    ownerId: String,
    limit: Int? = 15,
    skip: Int? = 0,
    searchOptions: PokemonSearchOptions = PokemonSearchOptions(),
    aggregation: MutableList<Bson> = mutableListOf()
  ): List<PokemonWithOnlyObjectId> {
    if (skip != null) aggregation.add(skip(skip))
    if (limit != null) aggregation.add(limit(limit))
    aggregation.add(project(PokemonWithOnlyObjectId::_id))
    val result = collection.aggregate<PokemonWithOnlyObjectId>(
      match(OwnedPokemon::ownerId eq ownerId),
      *searchOptions.pipeline,
      *aggregation.toTypedArray(),
    )
    return result.toList()
  }

  suspend fun getPokemonCount(
    ownerId: String,
    searchOptions: PokemonSearchOptions = PokemonSearchOptions(),
    aggregation: MutableList<Bson> = mutableListOf()
  ): Int {
    val result = collection.aggregate<CountResult>(
      match(OwnedPokemon::ownerId eq ownerId),
      *PokemonSearchOptions(
        PokemonOrder.DEFAULT,
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

  suspend fun setNickname(pokemon: OwnedPokemon, nickname: String?) {
    pokemon.nickname = if (nickname.isNullOrBlank()) null else nickname
    collection.updateOne(OwnedPokemon::_id eq pokemon._id, set(OwnedPokemon::nickname setTo pokemon.nickname))
  }

  suspend fun releasePokemon(pokemon: OwnedPokemon, session: ClientSession) {
    releasedPokemonCollection.insertOne(session, pokemon)
    collection.deleteOne(session, OwnedPokemon::_id eq pokemon._id)
  }

  suspend fun addEffort(
    pokemon: OwnedPokemon,
    stat: Stat,
    count: Int,
    session: ClientSession
  ): Boolean {
    val statField = when (stat.id) {
      Stat.hp.id -> PokemonStats::hp
      Stat.attack.id -> PokemonStats::attack
      Stat.defense.id -> PokemonStats::defense
      Stat.specialAttack.id -> PokemonStats::specialAttack
      Stat.specialDefense.id -> PokemonStats::specialDefense
      Stat.speed.id -> PokemonStats::speed
      else -> throw IllegalArgumentException("Unknown stat found: ${stat.id}")
    }
    var statEV = statField.get(pokemon.evs)

    if (statEV >= 252) return false

    val requiredUntilMax = 252 - statEV
    if (count > requiredUntilMax) return false
    statEV += count

    statField.set(pokemon.evs, statEV)

    collection.updateOne(
      session,
      OwnedPokemon::_id eq pokemon._id,
      set(
        OwnedPokemon::evs setTo pokemon.evs
      )
    )

    return true
  }

  @Suppress("unused")
  suspend fun resetEffort(
    pokemon: OwnedPokemon,
    stat: Stat,
  ) {
    val statField = when (stat.id) {
      Stat.hp.id -> PokemonStats::hp
      Stat.attack.id -> PokemonStats::attack
      Stat.defense.id -> PokemonStats::defense
      Stat.specialAttack.id -> PokemonStats::specialAttack
      Stat.specialDefense.id -> PokemonStats::specialDefense
      Stat.speed.id -> PokemonStats::speed
      else -> throw IllegalArgumentException("Unknown stat found: ${stat.id}")
    }

    statField.set(pokemon.evs, 0)

    collection.updateOne(
      OwnedPokemon::_id eq pokemon._id,
      set(
        OwnedPokemon::evs setTo pokemon.evs
      )
    )
  }

  suspend fun levelUpAndEvolveIfPossible(
    pokemon: OwnedPokemon,
    usedItemId: Int? = null,
    gainedXp: Int? = null,
    beingTradedFor: List<Int>? = null,
    updateInDb: Boolean = true,
    clientSession: ClientSession? = null
  ): Pair<Boolean, Boolean> {
    var leveledUp = false
    var evolved = false

    if (pokemon.heldItemId != VoidStoneItem.id) {
      if (gainedXp != null) {
        pokemon.xp += gainedXp
      }

      var requiredXpToLevelUp = pokemon.requiredXpToLevelUp()
      while (pokemon.level < 100 && requiredXpToLevelUp <= pokemon.xp) {
        leveledUp = true
        pokemon.xp -= requiredXpToLevelUp
        pokemon.level++
        requiredXpToLevelUp = pokemon.requiredXpToLevelUp()
      }
    }

    if (pokemon.id !in Pokemon.dontEvolveFrom && pokemon.heldItemId != 206) { // 206 = everstone
      val evolution = pokemon.data.nextEvolutions.map { EvolutionChain.details(it) }.find { evolutionDetails ->
        if (evolutionDetails?.evolvedSpeciesId in Pokemon.dontEvolveInto) return@find false

        val isGenderOk =
          if (evolutionDetails?.genderId != 0) if (evolutionDetails?.genderId == 1) pokemon.gender == 0 else if (evolutionDetails?.genderId == 2) pokemon.gender == 1 else true else true
        val isHeldItemOk =
          if (evolutionDetails?.heldItemId != 0) evolutionDetails?.heldItemId == pokemon.heldItemId else true
        val isKnownMoveOk =
          if (evolutionDetails?.knownMoveId != 0) pokemon.moves.contains(evolutionDetails?.knownMoveId) else true
        val isLevelUpOk =
          if (evolutionDetails?.evolutionTriggerId == 1) leveledUp else true
        val isMinimumLevelOk =
          (evolutionDetails?.minimumLevel ?: 0) <= pokemon.level
        val isTimeOk =
          if (evolutionDetails?.timeOfDay != "") evolutionDetails?.timeOfDay == EvolutionUtils.getCurrentTime(LocalDateTime.now()) else true
        val isTradeStateOk =
          if (evolutionDetails?.evolutionTriggerId == 2)
          if (evolutionDetails.tradeSpeciesId != 0 && beingTradedFor != null) beingTradedFor.contains(evolutionDetails.tradeSpeciesId)
          else beingTradedFor != null
        else true
        val isTriggerItemOk =
          if (evolutionDetails?.evolutionTriggerId == 3) evolutionDetails.triggerItemId == usedItemId else true

        isGenderOk &&
            isHeldItemOk &&
            isKnownMoveOk &&
            isLevelUpOk &&
            isMinimumLevelOk &&
            isTimeOk &&
            isTradeStateOk &&
            isTriggerItemOk &&
            evolutionDetails?.knownMoveTypeId == 0 &&
            evolutionDetails.locationId == 0 &&
            evolutionDetails.partySpeciesId == 0 &&
            evolutionDetails.partyTypeId == 0 &&
            evolutionDetails.relativePhysicalStats == 0 &&
            //evolutionDetails.timeOfDay == "" &&
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
    if (pokemon.heldItemId != VoidStoneItem.id) {
      if (leveledUp || gainedXp != null) {
        updatesBson.add(
          set(
            OwnedPokemon::level setTo pokemon.level,
            OwnedPokemon::xp setTo pokemon.xp
          )
        )
      }
    }

    if (evolved) {
      updatesBson.add(set(OwnedPokemon::id setTo pokemon.id))
    }

    if (updatesBson.isNotEmpty() && updateInDb) {
      if (clientSession != null) {
        collection.updateOne(
          clientSession,
          OwnedPokemon::_id eq pokemon._id,
          combine(updatesBson)
        )
      } else {
        collection.updateOne(
          OwnedPokemon::_id eq pokemon._id,
          combine(updatesBson)
        )
      }
    }

    return Pair(leveledUp, evolved)
  }

  suspend fun giftPokemon(sender: User, receiver: User, pokemon: OwnedPokemon, session: ClientSession) {
    collection.updateOne(
      session,
      OwnedPokemon::_id eq pokemon._id,
      combine(
        set(
          OwnedPokemon::index setTo receiver.nextIndex,
          OwnedPokemon::ownerId setTo receiver.id
        ),
        if (pokemon.trainerId == null) set(OwnedPokemon::trainerId setTo sender.id)
        else EMPTY_BSON
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

  suspend fun reindexPokemon(
    ownerId: String,
    order: PokemonOrder,
    extraOps: suspend (session: ClientSession, pokemonCount: Int) -> Unit = { _, _ -> }
  ) {
    val sortProperty = order.getSortProperty()
    cache.getUserLock(ownerId).withCoroutineLock(5, TimeUnit.MINUTES) {
      var done = 0
      val session = database.startSession()
      session.use {
        it.startTransaction()
        do {
          val items = collection.aggregate<PokemonWithOnlyObjectId>(
            match(OwnedPokemon::ownerId eq ownerId),
            project(
              OwnedPokemon::_id from OwnedPokemon::_id,
              sortProperty from sortProperty
            ),
            sort(
              combine(
                if (order == PokemonOrder.POKEDEX || order == PokemonOrder.TIME) ascending(sortProperty)
                else descending(sortProperty),
                ascending(OwnedPokemon::_id)
              )
            ),
            project(OwnedPokemon::_id from OwnedPokemon::_id),
            skip(done),
            limit(Config.reindexChunkSize)
          )
            .allowDiskUse(true)
            .toList()
          if (items.isNotEmpty()) {
            val updates = items.mapIndexed { i, pokemon ->
              updateOne<OwnedPokemon>(
                OwnedPokemon::_id eq pokemon._id,
                set(OwnedPokemon::index setTo done + i)
              )
            }
            collection.bulkWrite(updates)
          }
          done += items.size
          if (items.size < Config.reindexChunkSize) break
        } while (true)
        extraOps(session, done)
        it.commitTransactionAndAwait()
      }
    }
  }

  suspend fun getEstimatedPokemonCount(): Long {
    return collection.estimatedDocumentCount()
  }

  @Suppress("UNUSED")
  suspend fun transferPokemon(
    context: ICommandContext,
    filter: Bson,
    update: Bson,
    updateOptions: UpdateOptions = UpdateOptions()
  ): UpdateResult {
    var result = UpdateResult.unacknowledged()
    val matchedIds = collection.findAndCast<PokemonWithOnlyObjectId>(filter)
      .projection(OwnedPokemon::_id from OwnedPokemon::_id)
      .toList()
      .map { it._id }

    val insertedId = database.transferLogCollection.insertOne(
      TransferLog(
        filter.json,
        update.json,
        context.author.id,
        context.channel.id,
        performedInGuild = context.guild?.id
      )
    ).insertedId!!.asObjectId().value.toId<TransferLog>()

    val chunks = matchedIds
      .chunked(Config.transferChunkSize)

    for (chunk in chunks) {
      val session = database.startSession()
      session.use {
        it.startTransaction()
        val updateResult = collection.updateMany(it, OwnedPokemon::_id `in` chunk, update, updateOptions)
        database.transferLogCollection.updateOne(
          it,
          TransferLog::_id eq insertedId,
          combine(
            pushEach(TransferLog::matchedIds, chunk),
            set(TransferLog::status setTo TransferLog.Status.STARTED)
          )
        )
        it.commitTransactionAndAwait()
        if (updateResult.wasAcknowledged()) {
          result = if (result.wasAcknowledged()) {
            UpdateResult.acknowledged(
              result.matchedCount + updateResult.matchedCount,
              result.modifiedCount + updateResult.modifiedCount,
              null
            )
          } else {
            UpdateResult.acknowledged(updateResult.matchedCount, updateResult.modifiedCount, null)
          }
        }
      }
    }

    database.transferLogCollection.updateOne(
      TransferLog::_id eq insertedId,
      combine(
        set(TransferLog::status setTo TransferLog.Status.COMPLETE)
      )
    )

    return result
  }

  suspend fun updateNature(pokemon: OwnedPokemon, newNature: String) {
    pokemon.nature = newNature
    collection.updateOne(
      OwnedPokemon::_id eq pokemon._id,
      set(OwnedPokemon::nature setTo newNature)
    )
  }

  data class PokemonSearchOptions(
    val order: PokemonOrder? = PokemonOrder.DEFAULT,
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

    val hasOptions
      get() = (order != null && order != PokemonOrder.DEFAULT)
          || favorites != null
          || nature != null
          || rarity != null
          || shiny != null
          || type != null
          || regex != null
          || searchQuery != null

    init {
      val rarities: MutableList<String> =
        (rarity?.lowercase()?.split(Regex(",( )?"))?.toMutableList() ?: mutableListOf())

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
        ids = (ids.ifEmpty { allIds }).intersect(Pokemon.getByTypes(types).map { it.id }.toSet())
      }

      (if (regex != null) Pokemon.searchRegex(regex) else if (searchQuery != null) Pokemon.search(searchQuery) else null)
        ?.map { it.id }?.let {
          searchIds = it.toSet()
        }

      if (order != PokemonOrder.DEFAULT) {
        orderBson = when (order) {
          PokemonOrder.IV -> {
            sort(descending(OwnedPokemon::totalIv))
          }
          PokemonOrder.LEVEL -> {
            sort(descending(OwnedPokemon::level))
          }
          PokemonOrder.POKEDEX -> {
            sort(ascending(OwnedPokemon::id))
          }
          PokemonOrder.TIME -> {
            sort(ascending(OwnedPokemon::timestamp))
          }
          else -> null
        }
      }
    }

    val pipeline: Array<Bson>
      get() {
        val aggregation = mutableListOf<Bson>()
        if (ids.isNotEmpty()) {
          aggregation.add(match(OwnedPokemon::id `in` ids.asIterable()))
        }
        if (regex != null || searchQuery != null) {
          aggregation.add(
            match(
              or(
                OwnedPokemon::id `in` searchIds,
                OwnedPokemon::nickname.regex(
                  (regex ?: searchQuery!!).toString(),
                  if (regex?.options?.contains(RegexOption.IGNORE_CASE) == true || regex == null) "i" else ""
                ),
              )
            )
          )

//          aggregation.add(
//            BsonDocument.parse(
//              buildJsonObject {
//                putJsonObject("\$match") {
//                  putJsonArray("\$or") {
//                    addJsonObject {
//                      putJsonObject(OwnedPokemon::nickname.name) {
//                        put("\$regex", (regex ?: searchQuery!!).toString())
//                        put(
//                          "\$options",
//                          if (regex?.options?.contains(RegexOption.IGNORE_CASE) == true || regex == null) "i" else ""
//                        )
//                      }
//                    }
//                    addJsonObject {
//                      putJsonObject(OwnedPokemon::id.name) {
//                        putJsonArray("\$in") {
//                          searchIds.forEach {
//                            add(it)
//                          }
//                        }
//                      }
//                    }
//                  }
//                }
//              }.toString()
//            )
//          )
        }
        if (favorites == true) {
          aggregation.add(match(OwnedPokemon::favorite eq true))
        }
        if (shiny == true) {
          aggregation.add(match(OwnedPokemon::shiny eq true))
        }
        if (nature != null) {
          val natureObj = Nature.getByName(nature)
          if (natureObj != null) {
            aggregation.add(match(OwnedPokemon::nature eq natureObj.name!!.name))
          }
        }
        if (!noSorting) aggregation.add(orderBson ?: sort(ascending(OwnedPokemon::index)))
        return aggregation.toTypedArray()
      }
  }
}
