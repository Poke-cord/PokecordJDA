package xyz.pokecord.bot.core.managers.database.repositories

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.reactivestreams.client.ClientSession
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.bson.BsonDocument
import org.litote.kmongo.*
import org.litote.kmongo.MongoOperator.*
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
import xyz.pokecord.bot.utils.jsonObject
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

    inventoryItemsCollection.createIndex(Indexes.ascending("ownerId"))
    inventoryItemsCollection.createIndex(Indexes.compoundIndex(Indexes.ascending("id"), Indexes.ascending("ownerId")))
  }

  private suspend fun getCacheUser(jdaUser: JDAUser): User? {
    val json = cacheMap.getAsync(jdaUser.id).awaitSuspending() ?: return null
    return Json.decodeFromString(json)
  }

  private suspend fun setCacheUser(user: User) {
    cacheMap.putAsync(user.id, Json.encodeToString(user)).awaitSuspending()
  }

  suspend fun getUser(jdaUser: JDAUser): User {
    var user: User? = getCacheUser(jdaUser)
    if (user == null) {
      user = collection.findOne(User::id eq jdaUser.id)
      if (user == null) {
        user = User(jdaUser.id, jdaUser.asTag)
      } else setCacheUser(user)
    }
    return user
  }

  suspend fun updateTag(userData: User, tag: String) {
    userData.tag = tag
    collection.updateOne(User::_id eq userData._id, set(User::tag setTo tag))
    setCacheUser(userData)
  }

  suspend fun incCredits(userData: User, amount: Number, session: ClientSession? = null) {
    userData.credits += amount.toInt()
    if (session == null) collection.updateOne(User::_id eq userData._id, inc(User::credits, amount))
    else collection.updateOne(session, User::_id eq userData._id, inc(User::credits, amount))
  }

  suspend fun incGems(userData: User, amount: Int, session: ClientSession? = null) {
    userData.gems += amount
    if (session == null) collection.updateOne(User::_id eq userData._id, inc(User::gems, amount))
    else collection.updateOne(session, User::_id eq userData._id, inc(User::gems, amount))
  }

  suspend fun incTokens(userData: User, amount: Int, session: ClientSession? = null) {
    userData.tokens += amount
    if (session == null) collection.updateOne(User::_id eq userData._id, inc(User::tokens, amount))
    else collection.updateOne(session, User::_id eq userData._id, inc(User::tokens, amount))
  }

  suspend fun incShinyRate(userData: User, amount: Int, session: ClientSession? = null) {
    userData.shinyRate += amount
    if (session == null) collection.updateOne(User::_id eq userData._id, inc(User::shinyRate, amount))
    else collection.updateOne(session, User::_id eq userData._id, inc(User::shinyRate, amount))
  }

  suspend fun givePokemon(
    userData: User,
    pokemonId: Int,
    select: Boolean = false,
    ivs: PokemonStats? = null,
    extraOps: suspend (session: ClientSession) -> Unit = {}
  ): OwnedPokemon {
    if (pokemonId < 1 || pokemonId > Pokemon.maxId) throw IllegalArgumentException("Pokemon ID $pokemonId is not in range 0 < $pokemonId < ${Pokemon.maxId}")
    val isUnsavedUser = userData.isDefault
    var ownedPokemon = OwnedPokemon(
      pokemonId,
      userData.nextPokemonIndices.first(),
      userData.id,
      Random.nextDouble(userData.shinyRate) <= 1
    )

    if (ivs != null) ownedPokemon = ownedPokemon.copy(ivs = ivs)

    if (select) userData.selected = ownedPokemon._id
    if (!ownedPokemon.shiny) userData.shinyRate -= 0.25

    val targetList = if (ownedPokemon.shiny) userData.caughtShinies else userData.caughtPokemon
    if (!targetList.contains(pokemonId)) targetList.add(pokemonId)
    userData.nextPokemonIndices.removeFirstOrNull()
    if (userData.nextPokemonIndices.isEmpty()) userData.nextPokemonIndices.add(ownedPokemon.index + 1)
    val session = database.startSession()
    session.use { clientSession ->
      clientSession.startTransaction()
      database.pokemonRepository.insertPokemon(ownedPokemon, clientSession)
      if (isUnsavedUser) {
        collection.insertOne(clientSession, userData)
      } else {
        collection.updateOne(
          clientSession,
          User::id eq userData.id,
          combine(
            set(User::nextPokemonIndices setTo userData.nextPokemonIndices),
            addToSet(if (ownedPokemon.shiny) User::caughtShinies else User::caughtPokemon, pokemonId),
            if (select) set(User::selected setTo ownedPokemon._id) else EMPTY_BSON,
            if (!ownedPokemon.shiny) inc(User::shinyRate, -0.25) else EMPTY_BSON
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
    collection.updateOne(User::_id eq userData._id, set(User::selected setTo userData.selected))
    setCacheUser(userData)
  }

  suspend fun addPokemonIndex(userData: User, index: Int, session: ClientSession) {
    if (!userData.nextPokemonIndices.contains(index)) {
      userData.nextPokemonIndices.add(index)
      userData.nextPokemonIndices.sort()
      collection.updateOne(
        session,
        User::id eq userData.id,
        set(User::nextPokemonIndices setTo userData.nextPokemonIndices)
      )
      setCacheUser(userData)
    }
  }

  suspend fun removePokemonIndex(userData: User, index: Int, session: ClientSession) {
    if (userData.nextPokemonIndices.contains(index)) {
      if (userData.nextPokemonIndices.size == 1) userData.nextPokemonIndices.add(1, index + 1)
      userData.nextPokemonIndices.remove(index)
      userData.nextPokemonIndices.sort()
      collection.updateOne(
        session,
        User::id eq userData.id,
        set(User::nextPokemonIndices setTo userData.nextPokemonIndices)
      )
      setCacheUser(userData)
    }
  }

  suspend fun releasePokemon(userData: User, pokemon: OwnedPokemon, clientSession: ClientSession) {
    val targetList = if (pokemon.shiny) userData.releasedShinies else userData.releasedPokemon
    if (!targetList.contains(pokemon.id)) {
      targetList.add(pokemon.id)
    }
    database.pokemonRepository.releasePokemon(pokemon, clientSession)
    collection.updateOne(
      clientSession,
      User::id eq userData.id,
      addToSet((if (pokemon.shiny) User::releasedShinies else User::releasedPokemon), pokemon.id)
    )
    setCacheUser(userData)
  }

  suspend fun addDexCatchEntry(userData: User, pokemon: OwnedPokemon) {
    val targetList = if (pokemon.shiny) userData.caughtShinies else userData.caughtPokemon
    if (!targetList.contains(pokemon.id)) {
      targetList.add(pokemon.id)
      collection.updateOne(
        User::_id eq userData._id,
        addToSet((if (pokemon.shiny) User::caughtShinies else User::caughtPokemon), pokemon.id)
      )
      setCacheUser(userData)
    }
  }

  suspend fun giftCredits(sender: User, receiver: User, amount: Int) {
    val session = database.startSession()
    session.use { clientSession ->
      clientSession.startTransaction()
      collection.updateOne(clientSession, User::_id eq sender._id, inc(User::credits, -amount))
      collection.updateOne(clientSession, User::_id eq receiver._id, inc(User::credits, amount))
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

  suspend fun consumeInventoryItem(inventoryItem: InventoryItem, session: ClientSession? = null) {
    if (inventoryItem.amount <= 1) {
      if (session == null) inventoryItemsCollection.deleteOne(InventoryItem::_id eq inventoryItem._id)
      else inventoryItemsCollection.deleteOne(session, InventoryItem::_id eq inventoryItem._id)
    } else {
      if (session == null) {
        inventoryItemsCollection.updateOne(
          InventoryItem::_id eq inventoryItem._id,
          inc(InventoryItem::amount, -1)
        )
      } else {
        inventoryItemsCollection.updateOne(
          session,
          InventoryItem::_id eq inventoryItem._id,
          inc(InventoryItem::amount, -1)
        )
      }
    }
  }

  suspend fun addInventoryItem(userId: String, itemId: Int, amount: Int = 1, session: ClientSession? = null) {
    val existingItem = getInventoryItem(userId, itemId)
    val inventoryItem = existingItem ?: InventoryItem(itemId, userId, 0)

    if (existingItem == null) {
      inventoryItem.amount++
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
    collection.updateOne(User::_id eq userData._id, set(User::progressPrivate setTo !userData.progressPrivate))
  }

  suspend fun getCreditLeaderboard(limit: Int = 10): List<User> {
    val cachedString = leaderboardCacheMap.getAsync("credit").awaitSuspending()
    return if (cachedString != null) {
      Json.decodeFromString(cachedString)
    } else {
      val leaderboard = collection.find(EMPTY_BSON).sort(descending(User::credits)).limit(limit).toList()
      leaderboardCacheMap.putAsync("credit", Json.encodeToString(leaderboard))
      leaderboard
    }
  }

  suspend fun getPokemonCountLeaderboard(limit: Int = 10): List<PokemonCountLeaderboardResult> {
    val cachedString = leaderboardCacheMap.getAsync("pokemon").awaitSuspending()
    return if (cachedString != null) {
      Json.decodeFromString(cachedString)
    } else {
      val leaderboard = collection.aggregate<PokemonCountLeaderboardResult>(
        listOf(
          pokemonCountLeaderboardGroupStage,
          pokemonCountLeaderboardProjectStage,
          pokemonCountLeaderboardSortStage,
          limit(limit)
        )
      )
        .allowDiskUse(true)
        .toList()
      leaderboardCacheMap.putAsync("pokemon", Json.encodeToString(leaderboard))
      leaderboard
    }
  }

  suspend fun setAgreedToTerms(userData: User, agreed: Boolean = true) {
    userData.agreedToTerms = agreed
    collection.updateOne(User::_id eq userData._id, set(User::agreedToTerms setTo userData.agreedToTerms))
  }

  suspend fun setDonationTier(userData: User, donationTier: Int) {
    userData.donationTier = donationTier
    collection.updateOne(User::_id eq userData._id, set(User::donationTier setTo userData.donationTier))
  }

  private val pokemonCountLeaderboardGroupStage = BsonDocument.parse(
    jsonObject {
      json("$group") {
        string("_id", "\$id")
        json("pokemonCount") {
          string("$max", "\$nextPokemonIndices")
        }
        json("nextPokemonIndices") {
          string("$first", "\$nextPokemonIndices")
        }
        json("tag") {
          string("$first", "\$tag")
        }
      }
    }.toString()
  )

  private val pokemonCountLeaderboardProjectStage = BsonDocument.parse(
    jsonObject {
      json("$project") {
        string("id", "\$_id")
        number("tag", 1)
        json("pokemonCount") {
          array("$subtract") {
            json {
              array("$arrayElemAt") {
                string("\$pokemonCount")
                number(0)
              }
            }
            json {
              array("$subtract") {
                json {
                  string("$size", "\$nextPokemonIndices")
                }
                number(1)
              }
            }
          }
        }
      }
    }.toString()
  )

  private
  val pokemonCountLeaderboardSortStage = BsonDocument.parse(
    jsonObject {
      json("$sort") {
        number("pokemonCount", -1)
      }
    }.toString()
  )

  @Serializable
  data class PokemonCountLeaderboardResult(val id: String, val tag: String, val pokemonCount: Int)
}
