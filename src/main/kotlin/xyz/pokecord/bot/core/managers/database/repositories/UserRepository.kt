package xyz.pokecord.bot.core.managers.database.repositories

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.reactivestreams.client.ClientSession
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.commitTransactionAndAwait
import org.redisson.api.RMapCacheAsync
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.Gift
import xyz.pokecord.bot.core.managers.database.models.InventoryItem
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.managers.database.models.User
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.utils.Json
import xyz.pokecord.bot.utils.PokemonStats
import xyz.pokecord.bot.utils.extensions.awaitSuspending
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import net.dv8tion.jda.api.entities.User as JDAUser

class UserRepository(
  database: Database,
  private val collection: CoroutineCollection<User>,
  private val inventoryItemsCollection: CoroutineCollection<InventoryItem>,
  private val cacheMap: RMapCacheAsync<String, String>,
  private val leaderboardCacheMap: RMapCacheAsync<String, String>,
) : Repository(database) {
  override suspend fun createIndexes() {
    collection.createIndex(Indexes.ascending("id"), IndexOptions().unique(true))
    collection.createIndex(Indexes.ascending("credits"))
    collection.createIndex(Indexes.ascending("pokemonCount"))

    inventoryItemsCollection.createIndex(Indexes.ascending("ownerId"))
    inventoryItemsCollection.createIndex(Indexes.compoundIndex(Indexes.ascending("id"), Indexes.ascending("ownerId")))
  }

  private suspend fun getCacheUser(userId: String): User? {
    val json = cacheMap.getAsync(userId).awaitSuspending() ?: return null
    return Json.decodeFromString(json)
  }

  private suspend fun setCacheUser(user: User) {
    if (user._isNew && !user.isDefault) {
      user._isNew = false
      collection.insertOne(user)
    }
    cacheMap.putAsync(user.id, Json.encodeToString(user.copy())).awaitSuspending()
  }

  suspend fun getUser(jdaUser: JDAUser): User {
    return getUser(jdaUser.id)
  }

  suspend fun getUser(userId: String, userTag: String = ""): User {
    var user: User? = getCacheUser(userId)
    if (user == null) {
      user = collection.findOne(User::id eq userId)
      if (user == null) {
        user = User(userId, userTag, _isNew = true)
      } else setCacheUser(user)
    }
    return user
  }

  suspend fun updateTag(userData: User, tag: String) {
    userData.tag = tag
    collection.updateOne(User::id eq userData.id, set(User::tag setTo tag))
    setCacheUser(userData)
  }

  suspend fun incCredits(userData: User, amount: Number, session: ClientSession? = null) {
    userData.credits += amount.toInt()
    if (session == null) collection.updateOne(User::id eq userData.id, inc(User::credits, amount))
    else collection.updateOne(session, User::id eq userData.id, inc(User::credits, amount))
    setCacheUser(userData)
  }

  suspend fun incGems(userData: User, amount: Int, session: ClientSession? = null) {
    userData.gems += amount
    if (session == null) collection.updateOne(User::id eq userData.id, inc(User::gems, amount))
    else collection.updateOne(session, User::id eq userData.id, inc(User::gems, amount))
    setCacheUser(userData)
  }

  suspend fun incTokens(userData: User, amount: Int, session: ClientSession? = null) {
    userData.tokens += amount
    if (session == null) collection.updateOne(User::id eq userData.id, inc(User::tokens, amount))
    else collection.updateOne(session, User::id eq userData.id, inc(User::tokens, amount))
    setCacheUser(userData)
  }

  suspend fun incShinyRate(userData: User, amount: Int, session: ClientSession? = null) {
    userData.shinyRate += amount
    if (userData.shinyRate < 1) userData.shinyRate = 1.0
    if (session == null) collection.updateOne(User::id eq userData.id, inc(User::shinyRate, amount))
    else collection.updateOne(session, User::id eq userData.id, inc(User::shinyRate, amount))
    setCacheUser(userData)
  }

  suspend fun givePokemon(
    userData: User,
    pokemonId: Int,
    select: Boolean = false,
    ivs: PokemonStats? = null,
    extraOps: suspend (session: ClientSession) -> Unit = {}
  ): OwnedPokemon {
    if (pokemonId < 1 || pokemonId > Pokemon.maxId) throw IllegalArgumentException("Pokemon ID $pokemonId is not in range 0 < $pokemonId < ${Pokemon.maxId}")
    val isUnsavedUser = userData.isDefault && userData._isNew

    if (userData.shinyRate < 1) userData.shinyRate = 4908.0

    var ownedPokemon = OwnedPokemon(
      pokemonId,
      userData.nextIndex,
      userData.id,
      Random.nextDouble(userData.shinyRate) <= 1
    )

    if (ivs != null) ownedPokemon = ownedPokemon.copy(ivs = ivs)

    if (select) userData.selected = ownedPokemon._id
    if (!ownedPokemon.shiny) userData.shinyRate -= 0.25
    else userData.shinyRate = 4908.0

    val targetList = if (ownedPokemon.shiny) userData.caughtShinies else userData.caughtPokemon
    if (!targetList.contains(pokemonId)) targetList.add(pokemonId)
    userData.nextIndex++
    val session = database.startSession()
    session.use { clientSession ->
      clientSession.startTransaction()
      database.pokemonRepository.insertPokemon(ownedPokemon, clientSession)
      if (isUnsavedUser) {
        if (userData._isNew) userData._isNew = false
        collection.insertOne(clientSession, userData)
      } else {
        collection.updateOne(
          clientSession,
          User::id eq userData.id,
          combine(
            inc(User::nextIndex, 1),
            addToSet(
              if (ownedPokemon.shiny) User::caughtShinies else User::caughtPokemon, pokemonId
            ),
            if (select) set(User::selected setTo ownedPokemon._id) else EMPTY_BSON,
            set(User::shinyRate setTo userData.shinyRate)
          )
        )
      }
      extraOps(clientSession)
      clientSession.commitTransactionAndAwait()
    }
    setCacheUser(userData)
    return ownedPokemon
  }

  suspend fun selectPokemon(userData: User, pokemon: OwnedPokemon) {
    userData.selected = pokemon._id
    collection.updateOne(User::id eq userData.id, set(User::selected setTo userData.selected))
    setCacheUser(userData)
  }

  suspend fun releasePokemon(userData: User, pokemon: OwnedPokemon, clientSession: ClientSession) {
    val targetList = if (pokemon.shiny) userData.releasedShinies else userData.releasedPokemon
    if (!targetList.contains(pokemon.id)) {
      targetList.add(pokemon.id)
    }
    collection.updateOne(
      clientSession,
      User::id eq userData.id,
      combine(
        addToSet((if (pokemon.shiny) User::releasedShinies else User::releasedPokemon), pokemon.id),
        inc(User::pokemonCount, -1)
      )
    )
    setCacheUser(userData)
  }

  suspend fun addDexCatchEntry(userData: User, pokemon: OwnedPokemon) {
    val targetList = if (pokemon.shiny) userData.caughtShinies else userData.caughtPokemon
    if (!targetList.contains(pokemon.id)) {
      targetList.add(pokemon.id)
      collection.updateOne(
        User::id eq userData.id,
        addToSet((if (pokemon.shiny) User::caughtShinies else User::caughtPokemon), pokemon.id)
      )
      setCacheUser(userData)
    }
  }

  suspend fun giftPokemon(sender: User, receiver: User, pokemon: OwnedPokemon, clientSession: ClientSession) {
    sender.pokemonCount--

    receiver.nextIndex++
    receiver.pokemonCount++

    collection.updateOne(
      clientSession,
      User::id eq sender.id,
      inc(User::pokemonCount, -1)
    )
    collection.updateOne(
      clientSession,
      User::id eq receiver.id,
      combine(
        inc(User::pokemonCount, 1),
        inc(User::nextIndex, 1)
      )
    )
    database.giftCollection.insertOne(clientSession, Gift(sender.id, receiver.id, 0, mutableListOf(pokemon._id)))
    setCacheUser(receiver)
  }

  suspend fun giftCredits(sender: User, receiver: User, amount: Int) {
    val session = database.startSession()
    session.use { clientSession ->
      clientSession.startTransaction()
      collection.updateOne(clientSession, User::id eq sender.id, inc(User::credits, -amount))
      collection.updateOne(clientSession, User::id eq receiver.id, inc(User::credits, amount))
      database.giftCollection.insertOne(clientSession, Gift(sender.id, receiver.id, amount, mutableListOf()))
      clientSession.commitTransactionAndAwait()
      sender.credits -= amount
      receiver.credits += amount
      setCacheUser(sender)
      setCacheUser(receiver)
    }
  }

  suspend fun getInventoryItems(userId: String) =
    inventoryItemsCollection.find(InventoryItem::ownerId eq userId).toList()

  suspend fun getInventoryItem(userId: String, id: Int) =
    inventoryItemsCollection.findOne(InventoryItem::ownerId eq userId, InventoryItem::id eq id)

  suspend fun consumeInventoryItem(inventoryItem: InventoryItem, amount: Int = 1, session: ClientSession? = null) {
    if (inventoryItem.amount <= 1) {
      if (session == null) inventoryItemsCollection.deleteOne(InventoryItem::_id eq inventoryItem._id)
      else inventoryItemsCollection.deleteOne(session, InventoryItem::_id eq inventoryItem._id)
    } else {
      if (session == null) {
        inventoryItemsCollection.updateOne(
          InventoryItem::_id eq inventoryItem._id,
          inc(InventoryItem::amount, -amount)
        )
      } else {
        inventoryItemsCollection.updateOne(
          session,
          InventoryItem::_id eq inventoryItem._id,
          inc(InventoryItem::amount, -amount)
        )
      }
    }
  }

  suspend fun addInventoryItem(userId: String, itemId: Int, amount: Int = 1, session: ClientSession? = null) {
    val existingItem = getInventoryItem(userId, itemId)
    val inventoryItem = existingItem ?: InventoryItem(itemId, userId, 0)

    if (existingItem == null) {
      inventoryItem.amount += amount
      if (session == null) inventoryItemsCollection.insertOne(inventoryItem)
      else inventoryItemsCollection.insertOne(session, inventoryItem)
    } else {
      if (session == null) {
        inventoryItemsCollection.updateOne(
          InventoryItem::_id eq inventoryItem._id,
          inc(InventoryItem::amount, amount)
        )
      } else {
        inventoryItemsCollection.updateOne(
          session,
          InventoryItem::_id eq inventoryItem._id,
          inc(InventoryItem::amount, amount)
        )
      }
    }
  }

  suspend fun togglePrivate(userData: User) {
    userData.progressPrivate = !userData.progressPrivate
    collection.updateOne(User::id eq userData.id, set(User::progressPrivate setTo userData.progressPrivate))
    setCacheUser(userData)
  }

  suspend fun getCreditLeaderboard(limit: Int = 10): List<User> {
    val cachedString = leaderboardCacheMap.getAsync("credit").awaitSuspending()
    return if (cachedString != null) {
      Json.decodeFromString(cachedString)
    } else {
      val leaderboard = collection.find(EMPTY_BSON).sort(descending(User::credits)).limit(limit).toList()
      leaderboardCacheMap.putAsync("credit", Json.encodeToString(leaderboard), 1, TimeUnit.HOURS)
      leaderboard
    }
  }

  suspend fun getPokemonCountLeaderboard(limit: Int = 10): List<PokemonCountLeaderboardResult> {
    val cachedString = leaderboardCacheMap.getAsync("pokemon").awaitSuspending()
    return if (cachedString != null) {
      Json.decodeFromString(cachedString)
    } else {
      val leaderboard =
        collection
          .findAndCast<PokemonCountLeaderboardResult>(EMPTY_BSON)
          .projection(
            combine(
              PokemonCountLeaderboardResult::id from User::id,
              PokemonCountLeaderboardResult::tag from User::tag,
              PokemonCountLeaderboardResult::pokemonCount from User::pokemonCount
            )
          )
          .sort(descending(PokemonCountLeaderboardResult::pokemonCount))
          .limit(limit).toList()
//      val leaderboard = collection.aggregate<PokemonCountLeaderboardResult>(
//        listOf(
//          pokemonCountLeaderboardGroupStage,
//          pokemonCountLeaderboardProjectStage,
//          pokemonCountLeaderboardSortStage,
//          limit(limit)
//        )
//      )
//        .allowDiskUse(true)
//        .toList()
      leaderboardCacheMap.putAsync("pokemon", Json.encodeToString(leaderboard), 1, TimeUnit.HOURS)
      leaderboard
    }
  }

  suspend fun setAgreedToTerms(userData: User, agreed: Boolean = true) {
    userData.agreedToTerms = agreed
    collection.updateOne(User::id eq userData.id, set(User::agreedToTerms setTo userData.agreedToTerms))
    setCacheUser(userData)
  }

  suspend fun setDonationTier(userData: User, donationTier: Int) {
    userData.donationTier = donationTier
    collection.updateOne(User::id eq userData.id, set(User::donationTier setTo userData.donationTier))
    setCacheUser(userData)
  }

  suspend fun setLastVoteTime(
    userData: User,
    lastVoteAt: Long = System.currentTimeMillis(),
    session: ClientSession? = null
  ) {
    userData.lastVoteAt = lastVoteAt
    if (session == null) collection.updateOne(User::id eq userData.id, set(User::lastVoteAt setTo userData.lastVoteAt))
    else collection.updateOne(session, User::id eq userData.id, set(User::lastVoteAt setTo userData.lastVoteAt))
    setCacheUser(userData)
  }

  suspend fun setLastBoostTime(
    userData: User,
    lastBoostAt: Long = System.currentTimeMillis(),
    session: ClientSession? = null
  ) {
    userData.lastBoostAt = lastBoostAt
    if (session == null) collection.updateOne(
      User::id eq userData.id,
      set(User::lastBoostAt setTo userData.lastBoostAt)
    )
    else collection.updateOne(session, User::id eq userData.id, set(User::lastBoostAt setTo userData.lastBoostAt))
    setCacheUser(userData)
  }

  suspend fun getEstimatedUserCount(): Long {
    return collection.estimatedDocumentCount()
  }

  suspend fun setBlacklisted(userData: User, blacklisted: Boolean) {
    userData.blacklisted = blacklisted
    collection.updateOne(User::id eq userData.id, set(User::blacklisted setTo userData.blacklisted))
    setCacheUser(userData)
  }

  suspend fun postReindex(userData: User, pokemonCount: Int, clientSession: ClientSession) {
    userData.pokemonCount = pokemonCount
    userData.nextIndex = pokemonCount
    collection.updateOne(
      clientSession,
      User::id eq userData.id,
      set(User::pokemonCount setTo pokemonCount, User::nextIndex setTo pokemonCount)
    )
    setCacheUser(userData)
  }

//  private val pokemonCountLeaderboardGroupStage = BsonDocument.parse(
//    jsonObject {
//      json("$group") {
//        string("_id", "\$id")
//        json("pokemonCount") {
//          string("$max", "\$nextPokemonIndices")
//        }
//        json("nextPokemonIndices") {
//          string("$first", "\$nextPokemonIndices")
//        }
//        json("tag") {
//          string("$first", "\$tag")
//        }
//      }
//    }.toString()
//  )
//
//  private val pokemonCountLeaderboardProjectStage = BsonDocument.parse(
//    jsonObject {
//      json("$project") {
//        string("id", "\$_id")
//        number("tag", 1)
//        json("pokemonCount") {
//          array("$subtract") {
//            json {
//              array("$arrayElemAt") {
//                string("\$pokemonCount")
//                number(0)
//              }
//            }
//            json {
//              array("$subtract") {
//                json {
//                  string("$size", "\$nextPokemonIndices")
//                }
//                number(1)
//              }
//            }
//          }
//        }
//      }
//    }.toString()
//  )
//
//  private val pokemonCountLeaderboardSortStage = BsonDocument.parse(
//    jsonObject {
//      json("$sort") {
//        number("pokemonCount", -1)
//      }
//    }.toString()
//  )

  @Serializable
  data class PokemonCountLeaderboardResult(val id: String, val tag: String, val pokemonCount: Int)
}
