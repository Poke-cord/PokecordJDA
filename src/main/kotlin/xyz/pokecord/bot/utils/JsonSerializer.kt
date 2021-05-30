package xyz.pokecord.bot.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.plus
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule

val Json = Json {
  serializersModule += IdKotlinXSerializationModule
}
