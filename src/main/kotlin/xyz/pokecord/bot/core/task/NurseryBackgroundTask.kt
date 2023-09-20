package xyz.pokecord.bot.core.task

import kotlinx.coroutines.*

class NurseryBackgroundTask {
  private val scope = CoroutineScope(Dispatchers.IO)


  init {
    scope.launch {
      startTask()
    }
  }

  @OptIn(DelicateCoroutinesApi::class)
  private suspend fun startTask() {
    GlobalScope.launch(Dispatchers.IO) {
      while(true) {
        performTask()
        delay(30 * 60 * 1000) // 30 minutes
      }
    }
  }

  private suspend fun performTask() {
    // Execute task here
  }


}