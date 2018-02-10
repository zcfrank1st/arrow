package arrow.mtl

import arrow.HK
import arrow.typeclasses.Awaitable
import arrow.typeclasses.MonadContinuation
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.RestrictsSuspension

@RestrictsSuspension
open class MonadFilterContinuation<F, A>(val MF: MonadFilter<F>, latch: Awaitable<HK<F, A>>, override val context: CoroutineContext) :
        MonadContinuation<F, A>(MF, latch, context) {

    /**
     * marker exception that interrupts the coroutine flow and gets captured
     * to provide the monad empty value
     */
    private object PredicateInterrupted : RuntimeException()

    override fun resumeWithException(exception: Throwable) {
        when (exception) {
            PredicateInterrupted -> returnedMonad = MF.empty()
            else -> super.resumeWithException(exception)
        }
    }

    /**
     * Short circuits monadic bind if `predicate == false` return the
     * monad `empty` value.
     */
    fun continueIf(predicate: Boolean): Unit {
        if (!predicate) throw PredicateInterrupted
    }

    /**
     * Binds only if the given predicate matches the inner value otherwise binds into the Monad `empty()` value
     * on `MonadFilter` instances
     */
    suspend fun <B> HK<F, B>.bindWithFilter(f: (B) -> Boolean): B {
        val b: B = bind { this }
        return if (f(b)) b else bind { MF.empty<B>() }
    }

}