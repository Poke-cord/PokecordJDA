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
