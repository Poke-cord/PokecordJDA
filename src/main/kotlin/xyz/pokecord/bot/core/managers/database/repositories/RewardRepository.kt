package xyz.pokecord.bot.core.managers.database.repositories

import com.mongodb.client.model.Indexes
import com.mongodb.reactivestreams.client.ClientSession
import org.litote.kmongo.Id
import org.litote.kmongo.`in`
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.VoteReward

class RewardRepository(
  database: Database,
  private val voteRewardCollection: CoroutineCollection<VoteReward>
) : Repository(database) {
  override suspend fun createIndexes() {
    voteRewardCollection.createIndex(Indexes.ascending("userId"))
  }

  suspend fun getVoteRewards(userId: String) = voteRewardCollection.find(VoteReward::userId eq userId).toList()

  suspend fun deleteVoteRewards(voteRewardIds: List<Id<VoteReward>>, session: ClientSession? = null) {
    if (session == null) voteRewardCollection.deleteMany(VoteReward::_id `in` voteRewardIds)
    else voteRewardCollection.deleteMany(session, VoteReward::_id `in` voteRewardIds)
  }

  suspend fun giveVoteReward(reward: VoteReward) {
    voteRewardCollection.insertOne(reward)
  }
}
