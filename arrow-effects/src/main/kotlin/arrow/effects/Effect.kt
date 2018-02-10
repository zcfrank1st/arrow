package arrow.effects

import arrow.HK
import arrow.TC
import arrow.core.Either
import arrow.core.Tuple2
import arrow.core.toT
import arrow.effects.data.internal.BindingCancellationException
import arrow.typeclass
import arrow.typeclasses.MonadError
import arrow.typeclasses.internal.Platform
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext
import kotlin.coroutines.experimental.startCoroutine

@typeclass
interface Effect<F> : Async<F>, TC {
    fun <A> runAsync(fa: HK<F, A>, cb: (Either<Throwable, A>) -> HK<F, Unit>): HK<F, Unit>
}

/**
 * Entry point for monad bindings which enables for comprehensions. The underlying impl is based on coroutines.
 * A coroutines is initiated and inside [MonadSuspendCancellableContinuation] suspended yielding to [Monad.flatMap]. Once all the flatMap binds are completed
 * the underlying monad is returned from the act of executing the coroutine
 *
 * This one operates over [MonadError] instances that can support [Throwable] in their error type automatically lifting
 * errors as failed computations in their monadic context and not letting exceptions thrown as the regular monad binding does.
 *
 * This operation is cancellable by calling invoke on the [Disposable] return.
 * If [Disposable.invoke] is called the binding result will become a lifted [BindingCancellationException].
 */
fun <F, B> Effect<F>.bindingEffect(cc: CoroutineContext = EmptyCoroutineContext, c: suspend MonadSuspendCancellableContinuation<F, *>.() -> B): Tuple2<HK<F, B>, Disposable> {
    val continuation = MonadSuspendCancellableContinuation<F, B>(this, Platform.awaitableLatch(), cc)
    val coro: suspend () -> HK<F, B> = { pure(c(continuation)).also { continuation.resolve(Either.Right(it)) } }
    return this.async<B> { callback ->
        coro.startCoroutine(continuation)
        continuation.awaitNonBlocking(
                { callback(Either.Left(it)) },
                { runAsync(continuation.returnedMonad()) { res: Either<Throwable, B> -> invoke { callback(res) } } })
    } toT continuation.disposable()
}
