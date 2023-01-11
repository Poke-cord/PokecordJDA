package xyz.pokecord.bot.core.structures.store.packages

import org.litote.kmongo.coroutine.abortTransactionAndAwait
import org.litote.kmongo.coroutine.commitTransactionAndAwait
import xyz.pokecord.bot.core.managers.database.models.User
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.pokemon.items.RedeemItem
import xyz.pokecord.bot.utils.Config

object RolesPackage : Package() {
  override val id = "roles"
  override val items: List<Item> = listOf(
    RoleItem(
      "apprentice_trainer",
      0.99,
      "739988143427682374",
      1,
      1000,
      1,
      RedeemItem.Redeems.Celestial.id
    ),
    RoleItem(
      "intermediate_trainer",
      4.99,
      "739988157701030048",
      2,
      5000,
      1,
      RedeemItem.Redeems.Stellar.id
    ),
    RoleItem(
      "experienced_trainer",
      9.99,
      "739988165041061998",
      3,
      15000,
      3,
      RedeemItem.Redeems.Stellar.id
    ),
    RoleItem(
      "master_trainer",
      14.99,
      "739988169113731182",
      4,
      25000,
      3,
      RedeemItem.Redeems.Fanatical.id
    ),
    RoleItem(
      "legendary_trainer",
      24.99,
      "748017788932849744",
      5,
      35000,
      4,
      RedeemItem.Redeems.Fanatical.id
    ),
    RoleItem(
      "mythical_trainer",
      49.99,
      "748017810394972260",
      6,
      50000,
      5,
      RedeemItem.Redeems.Fanatical.id
    )
  )

  override suspend fun giveReward(bot: Bot, userData: User, item: Item): Boolean {
    if (item !is RoleItem) return false
    val session = bot.database.startSession()
    session.use { clientSession ->
      clientSession.startTransaction()
      if (userData.donationTier < item.donationTier) {
        bot.database.userRepository.setDonationTier(
          userData,
          item.donationTier,
          clientSession
        )
      }
      if (!bot.database.userRepository.incCredits(userData, item.credits, clientSession)) {
        clientSession.abortTransactionAndAwait()
        return false
      }
      val allowedRedeems = RedeemItem.redeemMap.filter { entry -> entry.key >= item.minRedeemId }.keys
      repeat(item.redeemCount) {
        val randomRedeemId = allowedRedeems.random()
        bot.database.userRepository.addInventoryItem(userData.id, randomRedeemId, 1, clientSession)
      }
      clientSession.commitTransactionAndAwait()
    }
    if (!bot.discordRestClient.giveMemberRole(Config.mainServer, userData.id, item.roleId)) {
      bot.logger.error("Failed to give role ${item.roleId} to ${userData.id}")
    }
    return true
  }

  class RoleItem(
    id: String,
    price: Number,
    val roleId: String,
    val donationTier: Int,
    val credits: Int,
    val redeemCount: Int,
    val minRedeemId: Int
  ) : Item(id, price)
}
