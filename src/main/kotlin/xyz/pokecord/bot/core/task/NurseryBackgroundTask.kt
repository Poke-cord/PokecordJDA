package xyz.pokecord.bot.core.task
import xyz.pokecord.bot.core.managers.database.repositories.DaycareRepository


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
    val repo = DaycareRepository(dayCare)
    repo.giveExpToAllPokemon(100)
  }


}