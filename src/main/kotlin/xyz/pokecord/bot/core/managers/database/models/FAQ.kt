package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import xyz.pokecord.bot.utils.FAQTranslation

@Serializable
data class FAQ(
  val id: String,
  val keywords: List<String>,
  val translations: List<FAQTranslation>,
  @Contextual val _id: Id<FAQ> = newId()
)
