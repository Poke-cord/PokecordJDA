package xyz.pokecord.migration

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.bson.*
import org.bson.types.ObjectId
import org.litote.kmongo.Id
import org.litote.kmongo.ascending
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.aggregate
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.id.IdGenerator
import org.litote.kmongo.id.toId
import org.litote.kmongo.newId
import org.litote.kmongo.reactivestreams.KMongo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.pokecord.bot.core.managers.I18n
import xyz.pokecord.bot.core.managers.database.models.*
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.utils.FAQTranslation
import xyz.pokecord.bot.utils.PokemonStats
import kotlin.collections.set
import kotlin.random.Random

object Migration {
  val logger: Logger = LoggerFactory.getLogger(Migration::class.java)

  private fun getPokemonFromDocument(bsonDocument: BsonDocument): OwnedPokemon {
    val objectId: ObjectId = if (bsonDocument.isObjectId("_id")) bsonDocument.getObjectId("_id").value else ObjectId()
    val originalId: ObjectId? =
      if (bsonDocument.isObjectId("originalId")) bsonDocument.getObjectId("originalId").value else null

    val favorite =
      if (bsonDocument.isBoolean("favorite")) bsonDocument.getBoolean("favorite").value else false

    val rewardClaimed =
      if (bsonDocument.isBoolean("rewardClaimed")) bsonDocument.getBoolean("rewardClaimed").value else false
    val shiny =
      if (bsonDocument.isBoolean("shiny")) bsonDocument.getBoolean("shiny").value else false
    val sticky =
      if (bsonDocument.isBoolean("sticky")) bsonDocument.getBoolean("sticky").value else false

    val id =
      if (bsonDocument.isNumber("id")) bsonDocument.getNumber("id")
        .intValue() else throw Exception("Non-number id found for pokemon ${objectId.toHexString()}")
    val xp =
      if (bsonDocument.isNumber("xp")) bsonDocument.getNumber("xp")
        .intValue() else throw Exception("Non-number xp found for pokemon ${objectId.toHexString()}")
    var gender =
      if (bsonDocument.isNumber("gender")) bsonDocument.getNumber("gender").intValue() else -99999
    if (gender == -99999) {
      val genderRate = Pokemon.getById(id)?.species?.genderRate
        ?: throw Exception("Failed to determine gender rate for pokemon ${objectId.toHexString()}")
      gender = if (genderRate == -1) -1
      else {
        if (Random.nextFloat() < (genderRate * 12.5) / 100) 0 else 1
      }
    }
    val heldItemId =
      if (bsonDocument.isNumber("heldItemId")) bsonDocument.getNumber("heldItemId").intValue()
      else 0
    val level =
      if (bsonDocument.isNumber("level")) bsonDocument.getNumber("level")
        .intValue() else throw Exception("Non-number level found for pokemon ${objectId.toHexString()}")

    val moves =
      if (bsonDocument.isArray("moves")) bsonDocument.getArray("moves").values.map { value -> if (value.isNull) 0 else value.asInt32().value }
        .toIntArray()
      else intArrayOf()

    var nickname: String? = if (bsonDocument.isString("nickname")) bsonDocument.getString("nickname").value else null
    val name = Pokemon.getById(id)?.species?.name?.name
    if (nickname == name) nickname = null
    val nature =
      if (bsonDocument.isString("nature")) bsonDocument.getString("nature").value else throw Exception("Non-string nature found for pokemon ${objectId.toHexString()}")
    val trainerId =
      if (bsonDocument.isString("trainerId")) bsonDocument.getString("trainerId").value else throw Exception("Non-string trainerId found for pokemon ${objectId.toHexString()}")
    val ownerId =
      if (bsonDocument.isString("ownerId")) bsonDocument.getString("ownerId").value else throw Exception("Non-string ownerId found for pokemon ${objectId.toHexString()}")

    val ivs = if (bsonDocument.isDocument("ivs")) bsonDocument.getDocument("ivs") else null
    val evs = if (bsonDocument.isDocument("evs")) bsonDocument.getDocument("evs") else null

    val pokemonStats =
      if (ivs == null) {
        PokemonStats(
          Random.nextInt(32),
          Random.nextInt(32),
          Random.nextInt(32),
          Random.nextInt(32),
          Random.nextInt(32),
          Random.nextInt(32)
        )
      } else {
        val attack =
          if (ivs.isNumber("attack")) ivs.getNumber("attack").intValue() else Random.nextInt(32)
        val defense =
          if (ivs.isNumber("defense")) ivs.getNumber("defense").intValue() else Random.nextInt(
            32
          )
        val hp =
          if (ivs.isNumber("hp")) ivs.getNumber("hp").intValue() else Random.nextInt(32)
        val specialAttack =
          if (ivs.isNumber("special-attack")) ivs.getNumber("special-attack")
            .intValue() else Random.nextInt(32)
        val specialDefense =
          if (ivs.isNumber("special-defense")) ivs.getNumber("special-defense")
            .intValue() else Random.nextInt(32)
        val speed =
          if (ivs.isNumber("speed")) ivs.getNumber("speed").intValue() else Random.nextInt(32)

        PokemonStats(attack, defense, hp, specialAttack, specialDefense, speed)
      }

      val evStats =
        if (evs == null) {
          PokemonStats(
            0,
            0,
            0,
            0,
            0,
            0
          )
        } else {
          val attack =
            if (evs.isNumber("attack")) evs.getNumber("attack").intValue() else 0
          val defense =
            if (evs.isNumber("defense")) evs.getNumber("defense").intValue() else 0
          val hp =
            if (evs.isNumber("hp")) evs.getNumber("hp").intValue() else 0
          val specialAttack =
            if (evs.isNumber("special-attack")) evs.getNumber("special-attack")
              .intValue() else 0
          val specialDefense =
            if (evs.isNumber("special-defense")) evs.getNumber("special-defense")
              .intValue() else 0
          val speed =
            if (evs.isNumber("speed")) evs.getNumber("speed").intValue() else 0

          PokemonStats(attack, defense, hp, specialAttack, specialDefense, speed)
        }

    return OwnedPokemon(
      id,
      0,
      ownerId,
      shiny,
      trainerId,
      level,
      nature,
      pokemonStats,
      evStats,
      xp,
      gender,
      heldItemId,
      moves.toMutableList(),
      favorite,
      rewardClaimed,
      objectId.date.time,
      sticky,
      nickname,
      null,
      IdGenerator.defaultGenerator.create(objectId.toHexString()).cast(),
      originalId?.toHexString()?.let { IdGenerator.defaultGenerator.create(it) }?.cast(),
    )
  }

  private suspend fun addIndices(
    newDatabase: CoroutineDatabase,
    newPokemonCollection: CoroutineCollection<OwnedPokemon>
  ) {
    val indicesCollection = newDatabase.getCollection<IndexEntry>("pokemon_indices")

    if (indicesCollection.countDocuments() < newPokemonCollection.countDocuments()) {
      val createIndicesPipeline = buildJsonArray {
        addJsonObject {
          putJsonObject("\$sort") {
            put("timestamp", 1)
            put("_id", 1)
          }
        }
        addJsonObject {
          putJsonObject("\$group") {
            put("_id", "\$ownerId")
            putJsonObject("ids") {
              put("\$push", "\$_id")
            }
          }
        }
        addJsonObject {
          putJsonObject("\$unwind") {
            put("path", "\$ids")
            put("includeArrayIndex", "idx")
          }
        }
        addJsonObject {
          putJsonObject("\$project") {
            put("_id", "\$ids")
            putJsonObject("index") {
              put("\$toInt", "\$idx")
            }
          }
        }
        addJsonObject {
          put("\$out", indicesCollection.namespace.collectionName)
        }
      }

      newPokemonCollection.aggregate<Any>(
        createIndicesPipeline.toString()
      ).allowDiskUse(true).toCollection()

      logger.info("Exported indices to another collection.")
    } else {
      logger.info("Indices collection is already up-to-date.")
    }

//    val updates = mutableListOf<WriteModel<OwnedPokemon>>()
//    val bufferSize = 8192
//    var totalUpdated = 0
//
//    indicesCollection.find().batchSize(bufferSize).noCursorTimeout(true).consumeEach { indexEntry ->
//      updates.add(
//        updateOne<OwnedPokemon>(
//          OwnedPokemon::_id eq indexEntry._id,
//          set(OwnedPokemon::index setTo indexEntry.index + 1)
//        )
//      )
//
//      if (updates.size >= bufferSize) {
//        logger.info("Updating ${updates.size} documents.")
//        val bulkWriteResult = newPokemonCollection.bulkWrite(updates)
//        if (bulkWriteResult.wasAcknowledged()) {
//          totalUpdated += bulkWriteResult.modifiedCount
//          logger.info("Total $totalUpdated documents updated.")
//          updates.clear()
//        } else {
//          logger.info("Bulk write failed")
//        }
//      }
//    }

    indicesCollection.aggregate<Any>(
      buildJsonArray {
        addJsonObject {
          putJsonObject("\$merge") {
            put("into", newPokemonCollection.namespace.collectionName)
            put("on", "_id")
          }
        }
      }.toString()
    ).allowDiskUse(true).toCollection()
    println("Adding indices finished!")
  }

  private suspend fun convertPokemonStructure(
    oldPokemonCollection: MongoCollection<Document>,
    newPokemonCollection: CoroutineCollection<OwnedPokemon>
  ): Map<String, Int> {
    val bufferSize = 8192
    var totalInserted = 0

    val indexMap = mutableMapOf<String, Int>()

    val cursor =
      oldPokemonCollection.find(
//        BsonDocument.parse(buildJsonObject {
//          putJsonObject("ownerId") {
//            putJsonArray("\$in") {
//              add("574951722645192734")
//              add("693914342625771551")
//              add("787542344688730152")
//            }
//          }
//        }.toString())
      ).sort(ascending(OwnedPokemon::id, OwnedPokemon::_id))
        .cursor()

    val docs = mutableListOf<OwnedPokemon>()
    cursor.asFlow().map { it.toBsonDocument() }.collect {
      try {
        val pokemon = getPokemonFromDocument(it)
        pokemon.index = indexMap[pokemon.ownerId] ?: 0
        indexMap[pokemon.ownerId] = pokemon.index + 1
        docs.add(pokemon)
        if (docs.size >= bufferSize) {
          newPokemonCollection.insertMany(docs)
          totalInserted += docs.size
          docs.clear()
          logger.info("Total $totalInserted documents inserted")
        }
      } catch (e: Throwable) {
        e.printStackTrace()
        logger.error(it.toBsonDocument().toJson())
      }
    }

    if (docs.isNotEmpty()) {
      newPokemonCollection.insertMany(docs)
      totalInserted += docs.size
      logger.info("Total $totalInserted documents inserted")
    }

    println("Converting pokemon finished!")
    return indexMap
  }

  private suspend fun convertFaqs(
    oldFaqsCollection: MongoCollection<Document>,
    newFaqsCollection: CoroutineCollection<FAQ>
  ) {
    oldFaqsCollection.find().asFlow().map { it.toBsonDocument() }.collect {
      try {
        val objectId = it.getObjectId("_id").value
        val answer = it.getString("answer").value
        val id = it.getString("id").value
        val question = it.getString("question").value
        val keywords =
          it.getArray("keywords").toArray().mapNotNull { item -> if (item is BsonString) item.value else null }

        newFaqsCollection.insertOne(
          FAQ(
            id,
            keywords,
            listOf(FAQTranslation(I18n.Language.EN_US, question, answer)),
            objectId.toId()
          )
        )
      } catch (e: BsonInvalidOperationException) {
        e.printStackTrace()
      }
    }
    println("FAQ conversion finished!")
  }

  private suspend fun convertSpawnChannels(
    oldSpawnChannelsCollection: MongoCollection<Document>,
    newSpawnChannelsCollection: CoroutineCollection<SpawnChannel>
  ) {
    oldSpawnChannelsCollection.find().asFlow().map { it.toBsonDocument() }.collect {
      try {
        val objectId = it.getObjectId("_id").value
        val id = it.getString("id").value
        val guildId = it.getString("guildId").value
        val requiredMessages = it.getInt32("requiredMessages").value
        val sentMessages = it.getInt32("sentMessages").value
        val spawned = it.getInt32("spawned").value
        newSpawnChannelsCollection.insertOne(
          SpawnChannel(
            id,
            guildId,
            requiredMessages,
            sentMessages,
            spawned,
            objectId.toId()
          )
        )
      } catch (e: BsonInvalidOperationException) {
        e.printStackTrace()
      }
    }
    println("Spawn channel conversion finished!")
  }

  private suspend fun convertUsers(
    indexMap: Map<String, Int>,
    oldUsersCollection: MongoCollection<Document>,
    newUsersCollection: CoroutineCollection<User>,
    newInventoryCollection: CoroutineCollection<InventoryItem>,
    newRewardsCollection: CoroutineCollection<VoteReward>
  ) {
    oldUsersCollection.find().asFlow().map { it.toBsonDocument() }.collect {
      try {
        val userId = if (it.isString("_id")) it.getString("_id").value else return@collect
        val tag = if (it.isString("tag")) it.getString("tag").value else null

        val blacklisted = if (it.isBoolean("blacklisted")) it.getBoolean("blacklisted").value else false
        val progressPrivate = if (it.isBoolean("progressPrivate")) it.getBoolean("progressPrivate").value else false

        val caughtPokemon =
          if (it.isArray("caughtPokemon")) it.getArray("caughtPokemon").toArray()
            .mapNotNull { item -> if (item is BsonInt32) item.value else null } else listOf()
        val caughtShinies =
          if (it.isArray("caughtShinies")) it.getArray("caughtShinies").toArray()
            .mapNotNull { item -> if (item is BsonInt32) item.value else null } else listOf()
        val releasedPokemon =
          if (it.isArray("released")) it.getArray("released").toArray()
            .mapNotNull { item -> if (item is BsonInt32) item.value else null } else listOf()
        val releasedShinies =
          if (it.isArray("releasedShinies")) it.getArray("releasedShinies").toArray()
            .mapNotNull { item -> if (item is BsonInt32) item.value else null } else listOf()

        val credits = if (it.isNumber("credits")) it.getNumber("credits").intValue() else 1000

        val donationTier = if (it.isInt32("donationTier")) it.getInt32("donationTier").value else 0
        val gems = if (it.isInt32("gems")) it.getInt32("gems").value else 0

        val selected = if (it.isObjectId("selected")) it.getObjectId("selected").value else null

        val inventory = if (it.isArray("inventory")) it.getArray("inventory") else null
//        val rewards = if (it.isDocument("rewards")) it.getDocument("rewards") else null

        val inventoryItems = mutableListOf<InventoryItem>()
        val voteRewards = mutableListOf<VoteReward>()

        inventory?.forEach { item ->
          if (item is BsonDocument) {
            try {
              val objectId = if (item.isObjectId("_id")) item.getObjectId("_id").value else null
              val amount = item.getInt32("amount").value
              val id = item.getInt32("id").value
              inventoryItems.add(InventoryItem(id, userId, amount, objectId?.toId() ?: newId()))
            } catch (e: BsonInvalidOperationException) {
              e.printStackTrace()
            }
          }
        }

        // TODO: migrate vote reward maybe
//        if (rewards != null) {
//          if (rewards.isArray("vote")) {
//            rewards.getArray("vote").toArray().forEach { item ->
//              try {
//                if (item is BsonDocument) {
//                  val id = if (item.isObjectId("_id")) item.getObjectId("_id").value else null
//                  val claimed = item.getBoolean("claimed").value
//                  val season = item.getInt32("season").value
//                  voteRewards.add(VoteReward(userId, season, claimed, id?.toId() ?: newId()))
//                }
//              } catch (e: BsonInvalidOperationException) {
//                e.printStackTrace()
//              }
//            }
//          }
//        }

        val nextIndex = indexMap.getOrElse(userId) { 0 }

        newUsersCollection.insertOne(
          User(
            userId,
            tag,
            blacklisted,
            caughtPokemon.toMutableList(),
            caughtShinies.toMutableList(),
            credits,
            gems,
            0,
            releasedPokemon.toMutableList(),
            releasedShinies.toMutableList(),
            progressPrivate,
            donationTier,
            selected?.toId(),
            nextIndex = nextIndex,
            pokemonCount = nextIndex
          )
        )
        if (inventoryItems.isNotEmpty()) newInventoryCollection.insertMany(inventoryItems)
        if (voteRewards.isNotEmpty()) newRewardsCollection.insertMany(voteRewards)
      } catch (e: BsonInvalidOperationException) {
        e.printStackTrace()
      }
    }
    println("Converting users finished!")
  }

  @JvmStatic
  fun main(args: Array<String>) {
    val connectionString = ConnectionString(System.getenv("MONGO_URL") ?: "mongodb://localhost/main")

    val client = MongoClients.create(connectionString)
    val oldDatabase = client.getDatabase(connectionString.database ?: "main")
    val oldFaqsCollection = oldDatabase.getCollection("faqs")
    val oldSpawnChannelsCollection = oldDatabase.getCollection("spawnchannels")
    val oldPokemonCollection = oldDatabase.getCollection("ownedpokemons")
    val oldUsersCollection = oldDatabase.getCollection("users")

    val kmongoClient = KMongo.createClient(connectionString).coroutine
    val newDatabase = kmongoClient.getDatabase(System.getenv("NEW_DB_NAME") ?: "test")
    val newFaqsCollection = newDatabase.getCollection<FAQ>()
    val newSpawnChannelsCollection = newDatabase.getCollection<SpawnChannel>()
    val newPokemonCollection = newDatabase.getCollection<OwnedPokemon>()
    val newUsersCollection = newDatabase.getCollection<User>()
    val newInventoryCollection = newDatabase.getCollection<InventoryItem>()
    val newRewardsCollection = newDatabase.getCollection<VoteReward>()

    runBlocking {
//      File("started").writeText("1")
      val indexMap = convertPokemonStructure(oldPokemonCollection, newPokemonCollection)
//      addIndices(newDatabase, newPokemonCollection)
//      File("indexMap.json").writeText(Json.encodeToString(indexMap))
      convertUsers(indexMap, oldUsersCollection, newUsersCollection, newInventoryCollection, newRewardsCollection)
//      File("users_done").writeText("1")
      convertFaqs(oldFaqsCollection, newFaqsCollection)
//      File("faqs_done").writeText("1")
      convertSpawnChannels(oldSpawnChannelsCollection, newSpawnChannelsCollection)
//      File("spawn_channels_done").writeText("1")
    }

    logger.info("All done!")
  }

  @Serializable
  data class IndexEntry(
    @Contextual val _id: Id<OwnedPokemon>,
    val index: Int
  )
}
