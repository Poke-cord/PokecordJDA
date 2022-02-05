package xyz.pokecord.utils

import org.redisson.api.RLockAsync
import xyz.pokecord.bot.utils.extensions.awaitSuspending
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

private val secureRandom = SecureRandom.getInstanceStrong()

class RCoroutineLock(
  private val rLockAsync: RLockAsync,
  private val rand: Long
) {
  suspend fun unlock(): Boolean {
    return try {
      rLockAsync.unlockAsync(rand).awaitSuspending()
      true
    } catch (e: IllegalMonitorStateException) {
      false
    }
  }
}

private suspend fun RLockAsync.coroutineLock(
  leaseTime: Long? = null,
  unit: TimeUnit = TimeUnit.SECONDS
): RCoroutineLock {
  val rand = secureRandom.nextLong()
  val coroutineLock = RCoroutineLock(this, rand)
  if (leaseTime != null) {
    lockAsync(leaseTime, unit, rand).awaitSuspending()
  } else {
    lockAsync(rand).awaitSuspending()
  }
  return coroutineLock
}

suspend fun RLockAsync.withCoroutineLock(
  leaseTime: Long? = null,
  unit: TimeUnit = TimeUnit.SECONDS,
  block: suspend () -> Unit
) {
  val lock = coroutineLock(leaseTime, unit)
  block()
  lock.unlock()
}
