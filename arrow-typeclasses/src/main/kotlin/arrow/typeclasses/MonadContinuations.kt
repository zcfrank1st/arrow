package arrow.typeclasses

import arrow.HK
import arrow.core.Either
import arrow.typeclasses.internal.Platform.awaitableLatch
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.RestrictsSuspension
import kotlin.coroutines.experimental.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.experimental.intrinsics.suspendCoroutineOrReturn
import kotlin.coroutines.experimental.startCoroutine

@RestrictsSuspension
open class MonadContinuation<F, A>(M: Monad<F>, private val latch: Awaitable<HK<F, A>>, override val context: CoroutineContext) :
        Continuation<HK<F, A>>, Monad<F> by M, Awaitable<HK<F, A>> by latch {

    override fun resume(value: HK<F, A>) {
        returnedMonad = value
    }

    override fun resumeWithException(exception: Throwable) {
        resolve(Either.left(exception))
    }

    protected fun bindingInContextContinuation(context: CoroutineContext): BindingInContextContinuation<HK<F, A>> =
            object : BindingInContextContinuation<HK<F, A>> {
                val latch: Awaitable<Unit> = awaitableLatch()

                override fun await() = latch.awaitBlocking().fold({ it }, { null })

                override val context: CoroutineContext = context

                override fun resume(value: HK<F, A>) {
                    returnedMonad = value
                    latch.resolve(Either.Right(Unit))
                }

                override fun resumeWithException(exception: Throwable) {
                    latch.resolve(Either.Left(exception))
                }
            }

    protected lateinit var returnedMonad: HK<F, A>

    open fun returnedMonad(): HK<F, A> =
            awaitBlocking().fold({ throw it }, { result -> flatMap(returnedMonad, { result }) })

    suspend fun <B> HK<F, B>.bind(): B = bind { this }

    suspend fun <B> (() -> B).bindIn(context: CoroutineContext): B =
            bindIn(context, this)

    open suspend fun <B> bind(m: () -> HK<F, B>): B = suspendCoroutineOrReturn { c ->
        val labelHere = c.stackLabels // save the whole coroutine stack labels
        returnedMonad = flatMap(m(), { x: B ->
            c.stackLabels = labelHere
            c.resume(x)
            returnedMonad
        })
        COROUTINE_SUSPENDED
    }

    open suspend fun <B> bindIn(context: CoroutineContext, m: () -> B): B = suspendCoroutineOrReturn { c ->
        val labelHere = c.stackLabels // save the whole coroutine stack labels
        val monadCreation: suspend () -> HK<F, A> = {
            flatMap(pure(m()), { xx: B ->
                c.stackLabels = labelHere
                c.resume(xx)
                returnedMonad
            })
        }
        val completion = bindingInContextContinuation(context)
        returnedMonad = flatMap(pure(Unit), {
            monadCreation.startCoroutine(completion)
            val error = completion.await()
            if (error != null) {
                throw error
            }
            returnedMonad
        })
        COROUTINE_SUSPENDED
    }
}
