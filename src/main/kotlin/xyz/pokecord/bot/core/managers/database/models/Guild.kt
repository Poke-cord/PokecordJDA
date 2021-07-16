package xyz.pokecord.bot.core.managers.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import xyz.pokecord.bot.core.managers.I18n

@Serializable
data class Guild(
  val id: String,
  var prefix: String? = null,
  var levelUpMessagesSilenced: Boolean = false,
  val language: I18n.Language? = null,
  @Contextual val _id: Id<Guild> = newId(),
  @Transient var _isNew: Boolean = false
) {
  val isDefault
    get() = (language == null && prefix == null && !levelUpMessagesSilenced)
}
