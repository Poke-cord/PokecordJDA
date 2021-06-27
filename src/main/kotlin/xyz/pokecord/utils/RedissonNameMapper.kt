package xyz.pokecord.utils

import org.redisson.api.NameMapper

class RedissonNameMapper(val prefix: String?) : NameMapper {
  override fun map(name: String): String {
    return if (prefix == null) name
    else "$prefix:$name"
  }

  override fun unmap(name: String): String {
    return when {
      prefix != null && name.startsWith("$prefix:") -> name.substring(prefix.length + 1)
      else -> name
    }
  }
}
