package katz

interface ResultMonad : Monad<Result.F> {
    override fun <A> pure(a: A): Result<A> = Result.pure(a)

    override fun <A, B> ap(fa: HK<Result.F, A>, ff: HK<Result.F, (A) -> B>): HK<Result.F, B> =
            fa.ev().ap(ff.ev())

    override fun <A, B> flatMap(fa: ResultKind<A>, f: (A) -> ResultKind<B>): Result<B> =
            fa.ev().flatMap { f(it).ev() }
}
