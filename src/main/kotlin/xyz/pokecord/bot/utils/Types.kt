package xyz.pokecord.bot.utils

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.pokecord.bot.core.managers.I18n

@Serializable
data class PokemonStats(
  val attack: Int,
  val defense: Int,
  val hp: Int,
  val specialAttack: Int,
  val specialDefense: Int,
  val speed: Int
) {
  val total = attack + defense + hp + specialAttack + specialDefense + speed
}

@Serializable
data class FAQTranslation(
  val language: I18n.Language,
  val question: String,
  val answer: String
)

@Serializable
data class CountResult(val count: Int)

sealed class PokemonResolvable(val data: Any?) {
  class Latest : PokemonResolvable("latest")
  class Int(data: kotlin.Int?) : PokemonResolvable(data)
}

@Serializable
data class CachedStaffMember(
  val username: String,
  val discriminator: String,
  val avatarUrl: String,
  val roleName: String,
  val rolePosition: Int
)

@Serializable
data class GuildMemberResponseUser(
  val id: String,
  val username: String,
  val avatar: String?,
  val discriminator: String,
  @SerialName("public_flags") val publicFlags: Int
)

@Serializable
data class GuildMemberResponse(
  val roles: List<String>,
  val avatar: String?,
  val user: GuildMemberResponseUser
)

@Serializable
data class GuildRoleResponse(
  val id: String,
  val name: String,
  val position: Int
)
