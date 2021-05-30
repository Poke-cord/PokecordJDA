package xyz.pokecord.bot.core.managers.database.repositories

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import xyz.pokecord.bot.core.managers.database.Database
import xyz.pokecord.bot.core.managers.database.models.FAQ
import xyz.pokecord.bot.utils.FAQTranslation

class FAQRepository(
  database: Database,
  private val collection: CoroutineCollection<FAQ>
) : Repository(database) {
  override suspend fun createIndexes() {
    collection.createIndex(Indexes.ascending("id"), IndexOptions().unique(true))
    collection.createIndex(Indexes.ascending("keywords"))
    collection.createIndex(Indexes.ascending("translations.question"))
    collection.createIndex(Indexes.ascending("translations.answer"))
  }

  suspend fun getFaqCount(keyword: String? = null): Long {
    var filter = EMPTY_BSON
    if (keyword != null) {
      filter = or(
        (FAQ::id eq keyword),
        (FAQ::keywords contains keyword),
        (FAQ::translations / FAQTranslation::question regex keyword),
        (FAQ::translations / FAQTranslation::answer regex keyword)
      )
    }
    return collection.countDocuments(filter)
  }

  suspend fun getFaqs(
    keyword: String? = null,
    skip: Int? = null,
    limit: Int? = null
  ): List<FAQ> {
    var filter = EMPTY_BSON
    if (keyword != null) {
      filter = or(
        (FAQ::id eq keyword),
        (FAQ::keywords contains keyword),
        (FAQ::translations / FAQTranslation::question regex keyword),
        (FAQ::translations / FAQTranslation::answer regex keyword)
      )
    }
    var findPublisher = collection.find(filter).sort(ascending(FAQ::_id))
    if (skip != null) findPublisher = findPublisher.skip(skip)
    if (limit != null) findPublisher = findPublisher.limit(limit)
    return findPublisher.toList()
  }
}
