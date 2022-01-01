package xyz.pokecord.bot.core.managers.database

import com.mongodb.ClientSessionOptions
import com.mongodb.ConnectionString
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

class Database(cache: Cache) {
  private val client: CoroutineClient
  val database: CoroutineDatabase

  private val auctionCollection: CoroutineCollection<Auction>
  private val battleCollection: CoroutineCollection<Battle>
  private val configCollection: CoroutineCollection<Config>
  private val faqCollection: CoroutineCollection<FAQ>
  private val guildCollection: CoroutineCollection<Guild>
  private val inventoryItemsCollection: CoroutineCollection<InventoryItem>
  private val marketCollection: CoroutineCollection<Listing>
  private val orderCollection: CoroutineCollection<Order>
  private val ownedPokemonCollection: CoroutineCollection<OwnedPokemon>
  private val spawnChannelCollection: CoroutineCollection<SpawnChannel>
  private val userCollection: CoroutineCollection<User>
  private val voteRewardsCollection: CoroutineCollection<VoteReward>
  private val tradeCollection: CoroutineCollection<Trade>

  val giftCollection: CoroutineCollection<Gift>
  val transferLogCollection: CoroutineCollection<TransferLog>

  val auctionRepository: AuctionsRepository
  val battleRepository: BattleRepository
  val configRepository: ConfigRepository
  val faqRepository: FAQRepository
  val guildRepository: GuildRepository
  val marketRepository: MarketRepository
  val orderRepository: OrderRepository
  val pokemonRepository: PokemonRepository
  val rewardRepository: RewardRepository
  val spawnChannelRepository: SpawnChannelRepository
  val userRepository: UserRepository
  val tradeRepository: TradeRepository

  init {
    val connectionString = ConnectionString(System.getenv("MONGO_URL") ?: "mongodb://localhost/main")
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
    faqCollection = database.getCollection()
    guildCollection = database.getCollection()
    inventoryItemsCollection = database.getCollection()
    marketCollection = database.getCollection()
    orderCollection = database.getCollection()
    ownedPokemonCollection = database.getCollection()
    spawnChannelCollection = database.getCollection()
    userCollection = database.getCollection()
    voteRewardsCollection = database.getCollection()
    tradeCollection = database.getCollection()

    giftCollection = database.getCollection()
    transferLogCollection = database.getCollection()

    auctionRepository = AuctionsRepository(this, auctionCollection, cache.auctionMap)
    battleRepository = BattleRepository(this, battleCollection, cache.battleRequestsMap)
    configRepository = ConfigRepository(this, configCollection)
    faqRepository = FAQRepository(this, faqCollection)
    guildRepository = GuildRepository(this, guildCollection, cache.guildMap)

    marketRepository = MarketRepository(this, marketCollection, cache.listingMap)
    orderRepository = OrderRepository(this, orderCollection)
    pokemonRepository = PokemonRepository(this, cache, ownedPokemonCollection)
    rewardRepository = RewardRepository(this, voteRewardsCollection)
    spawnChannelRepository =
      SpawnChannelRepository(this, spawnChannelCollection, cache.spawnChannelsMap, cache.guildSpawnChannelsMap)
    userRepository = UserRepository(this, userCollection, inventoryItemsCollection, cache.userMap, cache.leaderboardMap)
    tradeRepository = TradeRepository(this, tradeCollection)

    CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()).launch { createIndexes() }
  }

  private suspend fun createIndexes() {
    auctionRepository.createIndexes()
    battleRepository.createIndexes()
    faqRepository.createIndexes()
    guildRepository.createIndexes()
    marketRepository.createIndexes()
    orderRepository.createIndexes()
    pokemonRepository.createIndexes()
    rewardRepository.createIndexes()
    spawnChannelRepository.createIndexes()
    userRepository.createIndexes()
  }

  suspend fun startSession(options: ClientSessionOptions? = null) =
    if (options != null) client.startSession(options) else client.startSession()

  fun close() {
    client.close()
  }

  companion object {
    private val logger = LoggerFactory.getLogger(Database::class.java)
  }
}