package kategory.collections

import kategory.*

@higherkind
@deriving(
        Functor::class,
        Applicative::class,
        Monad::class,
        Foldable::class,
        Traverse::class,
        SemigroupK::class,
        MonoidK::class)
class PList<out A>() : PListKind<A> {

    fun <B> flatMap(f: (A) -> PListKind<B>): PList<B> = TODO()

    fun <B> ap(ff: PListKind<(A) -> B>): PList<B> = TODO()

    fun <B> map(f: (A) -> B): PList<B> = TODO()

    fun <B> foldL(b: B, f: (B, A) -> B): B = TODO()

    fun <B> foldR(lb: Eval<B>, f: (A, Eval<B>) -> Eval<B>): Eval<B> = TODO()

    fun <G, B> traverse(f: (A) -> HK<G, B>, GA: Applicative<G>): HK<G, PList<B>> = TODO()

    fun <B, Z> map2(fb: PListKind<B>, f: (Tuple2<A, B>) -> Z): PList<Z> = TODO()

    companion object {

        fun <A> pure(a: A): PList<A> = TODO()

        fun <A> empty(): PList<A> = TODO()

        fun <A, B> tailRecM(a: A, f: (A) -> PListKind<Either<A, B>>): PList<B> = TODO()

    }
}

fun <A> PList<A>.combineK(y: PListKind<A>): PList<A> = TODO()