package xyz.pokecord.bot.utils.extensions

fun <T> List<T>.randomChoice(
  weights: List<Double>? = null,
): T {
  if (weights != null) {
    val totalWeight = weights.sum()
    var r = Math.random() * totalWeight

    for (index in 0 until this.size - 1) {
      r -= weights[index]
//      println(r)
      if (r <= 0.0) return this[index]
    }
  }

  return this.random()
}
