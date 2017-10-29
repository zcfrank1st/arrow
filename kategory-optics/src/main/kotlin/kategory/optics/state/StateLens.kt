package kategory.optics.state

import kategory.*
import kategory.optics.Lens
import kategory.optics.PLens

fun <S, T, A, B> PLens<S, T, A, B>.toState() = State<S, A> { s ->
    s toT get(s)
}

fun <S, T, A, B> PLens<S, T, A, B>.extract() = toState()

inline fun <S, T, A, B, C> PLens<S, T, A, B>.extracts(crossinline f: (A) -> C) =
        extract().ev().map { f(it) }.ev()

fun <S, A> Lens<S, A>.mod(f: (A) -> A) = State<S, A> {
    val a = get(it)
    val b = f(a)
    set(it, b) toT b
}

fun <S, A> Lens<S, A>.mod_(f: (A) -> A) = State<S, Unit> {
    val a = get(it)
    val b = f(a)
    set(it, b) toT Unit
}