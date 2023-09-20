package xyz.pokecord.bot.core.managers.database

import com.mongodb.ClientSessionOptions
import com.mongodb.ConnectionString
import com.mongodb.ReadPreference
import com.mongodb.TransactionOptions
import com.mongodb.reactivestreams.client.ClientSession
import kotlinx.coroutines.*
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.slf4j.LoggerFactory
import xyz.pokecord.bot.core.managers.Cache
import xyz.pokecord.bot.core.managers.database.models.*
import xyz.pokecord.bot.core.managers.database.repositories.*
import java.util.concurrent.Executors
import kotlin.system.exitProcess

abstract class Database(cache: Cache) {
  private val client: CoroutineClient
  val database: CoroutineDatabase

  private val battleCollection: CoroutineCollection<Battle>
  private val configCollection: CoroutineCollection<Config>
  private val donateBotTransactionCollection: CoroutineCollection<DonateBotTransaction>
  private val faqCollection: CoroutineCollection<FAQ>
  private val guildCollection: CoroutineCollection<Guild>
  private val inventoryItemsCollection: CoroutineCollection<InventoryItem>
  private val orderCollection: CoroutineCollection<Order>
  private val ownedPokemonCollection: CoroutineCollection<OwnedPokemon>
  private val releaseCollection: CoroutineCollection<Release>
  private val spawnChannelCollection: CoroutineCollection<SpawnChannel>
  private val userCollection: CoroutineCollection<User>
  private val voteRewardsCollection: CoroutineCollection<VoteReward>

  val auctionCollection: CoroutineCollection<Auction>
  val giftCollection: CoroutineCollection<Gift>
  val marketCollection: CoroutineCollection<Listing>
  val tradeCollection: CoroutineCollection<Trade>
  val transferLogCollection: CoroutineCollection<TransferLog>

  val auctionRepository: AuctionsRepository
  val battleRepository: BattleRepository
  val configRepository: ConfigRepository
  val donateBotTransactionRepository: DonateBotTransactionRepository
  val faqRepository: FAQRepository
  val guildRepository: GuildRepository
  val marketRepository: MarketRepository
  val orderRepository: OrderRepository
  val pokemonRepository: PokemonRepository
  val releaseRepository: ReleaseRepository
  val rewardRepository: RewardRepository
  val spawnChannelRepository: SpawnChannelRepository
  val userRepository: UserRepository
  val tradeRepository: TradeRepository
  var daycareRepository: DaycareRepository

  init {
    val connectionString = ConnectionString(System.getenv("MONGO_URL") ?: "mongodb+srv://ADH:8032160ammy@cluster0.vpyplhr.mongodb.net/")
    client = KMongo.createClient(connectionString).coroutine
    database = client.getDatabase(connectionString.database ?: "main")

    runBlocking {
      try {
        withTimeout(30_000) {
          database.listCollectionNames()
        }
      } catch (e: TimeoutCancellationException) {
        logger.error("Failed to verify mongo connection after 30 seconds. Exiting...")
        exitProcess(1)
      }
    }

    auctionCollection = database.getCollection()
    battleCollection = database.getCollection()
    configCollection = database.getCollection()
    donateBotTransactionCollection = database.getCollection()
    faqCollection = database.getCollection()
    guildCollection = database.getCollection()
    inventoryItemsCollection = database.getCollection()
    marketCollection = database.getCollection()
    orderCollection = database.getCollection()
    ownedPokemonCollection = database.getCollection()
    releaseCollection = database.getCollection()
    spawnChannelCollection = database.getCollection()
    userCollection = database.getCollection()
    voteRewardsCollection = database.getCollection()
    tradeCollection = database.getCollection()
    daycareRepository = database.getCollection()

    giftCollection = database.getCollection()
    transferLogCollection = database.getCollection()

    auctionRepository = AuctionsRepository(this, auctionCollection, cache.auctionMap)
    battleRepository = BattleRepository(this, battleCollection, cache.battleRequestsMap)
    configRepository = ConfigRepository(this, configCollection)
    donateBotTransactionRepository = DonateBotTransactionRepository(this, donateBotTransactionCollection)
    faqRepository = FAQRepository(this, faqCollection)
    guildRepository = GuildRepository(this, guildCollection, cache.guildMap)

    marketRepository = MarketRepository(this, marketCollection, cache.listingMap)
    orderRepository = OrderRepository(this, orderCollection)
    pokemonRepository = PokemonRepository(this, cache, ownedPokemonCollection)
    releaseRepository = ReleaseRepository(this, releaseCollection)
    rewardRepository = RewardRepository(this, voteRewardsCollection)
    daycareRepository = DaycareRepository(this, daycareCollection)
    spawnChannelRepository =
      SpawnChannelRepository(
        this,
        spawnChannelCollection,
//        cache.spawnChannelsMap,
//        cache.guildSpawnChannelsMap
      )
    userRepository = UserRepository(this, userCollection, inventoryItemsCollection, cache.userMap, cache.leaderboardMap)
    tradeRepository = TradeRepository(this, tradeCollection)

    CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()).launch { createIndexes() }
  }

  private suspend fun createIndexes() {
    auctionRepository.createIndexes()
    battleRepository.createIndexes()
    donateBotTransactionRepository.createIndexes()
    faqRepository.createIndexes()
    guildRepository.createIndexes()
    marketRepository.createIndexes()
    orderRepository.createIndexes()
    pokemonRepository.createIndexes()
    rewardRepository.createIndexes()
    spawnChannelRepository.createIndexes()
    userRepository.createIndexes()
    daycareRepository.createIndexes()
  }

  suspend fun startSession(options: ClientSessionOptions? = null): ClientSession {
    val sessionOptionsBuilder = options?.let { ClientSessionOptions.builder(it) } ?: ClientSessionOptions.builder()
    sessionOptionsBuilder.defaultTransactionOptions(
      TransactionOptions.builder().readPreference(ReadPreference.primary()).build()
    )
    return client.startSession(sessionOptionsBuilder.build())
  }

  fun close() {
    client.close()
  }

  companion object {
    private val logger = LoggerFactory.getLogger(Database::class.java)
  }
}