package xyz.pokecord.bot.utils.extensions

import java.util.concurrent.CompletionStage
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <T> CompletionStage<T>.awaitSuspending(): T {
  return suspendCoroutine { cont: Continuation<T> ->
    whenComplete { result, exception ->
      if (exception == null) cont.resume(result)
      else cont.resumeWithException(exception)
    }
  }
}
