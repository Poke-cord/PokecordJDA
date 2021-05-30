package xyz.pokecord.bot.core.structures.store.packages

import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext

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

  override suspend fun giveReward(context: MessageReceivedContext, item: Item) {
    if (item !is RoleItem) return
    val userData = context.getUserData()
    if (userData.donationTier < item.donationTier) {
      context.bot.database.userRepository.setDonationTier(
        userData,
        item.donationTier
      )
    }
    // TODO: giveMemberRole(serverId, userData.id, item.roleId)
  }

  class RoleItem(
    id: String,
    price: Number,
    val roleId: String,
    val donationTier: Int
  ) : Item(id, price)
}
