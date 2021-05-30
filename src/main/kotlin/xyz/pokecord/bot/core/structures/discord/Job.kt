package xyz.pokecord.bot.core.structures.discord

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Job as CoroutineJob

abstract class Job : CoroutineScope {
  override val coroutineContext: CoroutineContext
    get() = job + Dispatchers.Default

  abstract val interval: Long
  abstract val name: String

  private val job = CoroutineJob()

  lateinit var module: Module

  var enabled = true
  var lastRunAt: Long = System.currentTimeMillis()

  fun start() {
    if (!module.enabled || !enabled) return
    val executeMethod = this::execute
    launch {
      while (isActive && enabled && module.enabled) {
        executeMethod()
        delay(interval)
      }
    }
  }

  abstract suspend fun execute(): Unit
}
