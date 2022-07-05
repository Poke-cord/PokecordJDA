package xyz.pokecord.bot.core.managers.database.repositories

import xyz.pokecord.bot.core.managers.database.Database

abstract class Repository(open val database: Database) {
  abstract suspend fun createIndexes()
}
