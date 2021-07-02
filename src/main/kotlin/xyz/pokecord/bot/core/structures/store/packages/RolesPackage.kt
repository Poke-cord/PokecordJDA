package xyz.pokecord.bot.core.structures.store.packages

import xyz.pokecord.bot.core.managers.database.models.User
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.utils.Config

object RolesPackage : Package() {
  override val id = "roles"
  override val items: List<Item> = listOf(
    RoleItem(
      "apprentice_trainer",
      0.99,
      "739988143427682374",
      1
    ),
    RoleItem(
      "intermediate_trainer",
      4.99,
      "739988157701030048",
      2
    ),
    RoleItem(
      "experienced_trainer",
      9.99,
      "739988165041061998",
      3
    ),
    RoleItem(
      "master_trainer",
      14.99,
      "739988169113731182",
      4
    ),
    RoleItem(
      "legendary_trainer",
      24.99,
      "748017788932849744",
      5
    ),
    RoleItem(
      "mythical_trainer",
      49.99,
      "748017810394972260",
      6
    )
  )

  override suspend fun giveReward(bot: Bot, userData: User, item: Item) {
    if (item !is RoleItem) return
    if (userData.donationTier < item.donationTier) {
      bot.database.userRepository.setDonationTier(
        userData,
        item.donationTier
      )
    }
    if (!bot.discordRestClient.giveMemberRole(Config.mainServer, userData.id, item.roleId)) {
      bot.logger.error("Failed to give role ${item.roleId} to ${userData.id}")
    }
  }

  class RoleItem(
    id: String,
    price: Number,
    val roleId: String,
    val donationTier: Int
  ) : Item(id, price)
}
