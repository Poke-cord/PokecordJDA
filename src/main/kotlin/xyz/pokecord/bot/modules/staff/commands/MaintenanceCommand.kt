package xyz.pokecord.bot.modules.staff.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.modules.staff.StaffCommand

class MaintenanceCommand : StaffCommand() {
  override val name = "Maintenance"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(optional = true) maintenance: Boolean?
  ) {
    if (maintenance != null) {
      module.bot.cache.setMaintenanceStatus(maintenance)
    } else {
      module.bot.cache.setMaintenanceStatus(!module.bot.maintenance)
    }
    context.reply(
      """
      Maintenance status set to ${maintenance ?: !module.bot.maintenance}.
      It may take up to 30 seconds for this change to be reflected across all shards.
    """.trimIndent()
    )
      .queue()
  }
}
