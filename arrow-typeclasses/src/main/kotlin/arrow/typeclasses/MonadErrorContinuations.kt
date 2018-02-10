package arrow.typeclasses

import arrow.HK
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.RestrictsSuspension

@RestrictsSuspension
open class MonadErrorContinuation<F, A>(val ME: MonadError<F, Throwable>, latch: Awaitable<HK<F, A>>, override val context: CoroutineContext) :
        MonadContinuation<F, A>(ME, latch, context), MonadError<F, Throwable> by ME {

    override fun returnedMonad(): HK<F, A> =
            awaitBlocking().fold({ raiseError(it) }, { result -> flatMap(returnedMonad, { result }) })
}
