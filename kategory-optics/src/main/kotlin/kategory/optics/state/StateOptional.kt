package kategory.optics.state

import kategory.*
import kategory.optics.Optional
import kategory.optics.POptional
import kategory.optics.modify

fun <S, T, A, B> POptional<S, T, A, B>.toState() = State<S, Option<A>> { s ->
    s toT getOption(s)
}

fun <S, T, A, B> POptional<S, T, A, B>.extract(): StateT<IdHK, S, Option<A>> = toState()

inline fun <S, T, A, B> POptional<S, T, A, B>.extracts(crossinline f: (A) -> B): StateT<IdHK, S, Option<B>> =
        extract().ev().map { it.map(f) }.ev()

fun <S, A> Optional<S, A>.mod(f: (A) -> A) = State<S, Option<A>> {
    modify(it, f) toT getOption(it)
}

fun <S, A> Optional<S, A>.mod_(f: (A) -> A) = State<S, Unit> {
    modify(it, f) toT Unit
}
