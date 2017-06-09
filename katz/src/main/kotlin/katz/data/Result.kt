package katz

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async

typealias ResultKind<A> = HK<Result.F, A>

fun <A> ResultKind<A>.ev(): Result<A> = this as Result<A>

class Result<out A> private constructor(private val value: Deferred<A>) : ResultKind<A> {

    class F private constructor()

    fun <B> ap(f: Result<(A) -> (B)>): Result<B> =
            Result(async(CommonPool) {
                f.value.await()(value.await())
            })

    fun <B> map(f: (A) -> (B)): Result<B> =
            Result(async(CommonPool) {
                f(value.await())
            })

    fun <B> flatMap(f: (A) -> Result<B>): Result<B> =
            Result(async(CommonPool) {
                f(value.await()).value.await()
            })

    companion object {
        @JvmStatic fun <A> pure(a: A): Result<A> = Result(async(CommonPool) { a })
        @JvmStatic fun <A> fromDeferred(a: Deferred<A>): Result<A> = Result(a)
    }
}
