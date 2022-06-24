package xyz.pokecord.bot.utils.extensions

import java.text.Normalizer

private val REGEX_REMOVE_ACCENTS = "\\p{InCombiningDiacriticalMarks}+".toRegex()

fun CharSequence.removeAccents(): String {
  val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
  return REGEX_REMOVE_ACCENTS.replace(temp, "")
}

val String.asTrainerId
  get() = Integer.toUnsignedString(this.hashCode() / (this.take(2).toIntOrNull() ?: 57))
    .take(if ((this.take(1).toIntOrNull() ?: 5) < 5) 5 else 6)

fun CharSequence.toIntArrayOrNull(): IntArray? {
  val parts = this.split(",")
  val intArray = parts.mapNotNull {
    if (it.contains("-")) {
      val innerParts = it.split("-")
      val first = innerParts.firstOrNull()?.toIntOrNull()
      val last = innerParts.lastOrNull()?.toIntOrNull()
      if (first != null && last != null) {
        IntRange(first, last).toList()
      } else {
        null
      }
    } else {
      it.toIntOrNull()?.let { int -> listOf(int) }
    }
  }.flatten().toIntArray()
  return if (intArray.isNotEmpty()) intArray
  else null
}
