package kategory.optics.state

import kategory.*
import kategory.optics.Lens
import kategory.optics.PLens

/**
 * Transforms a PLens into a State
 */
fun <S, T, A, B> PLens<S, T, A, B>.toState() = State<S, A> { s ->
    s toT get(s)
}

/**
 * Extracts the value viewed through the lens
 */
fun <S, T, A, B> PLens<S, T, A, B>.extract() = toState()

/**
 * Extracts the value viewed through the lens and applies [f] over it
 */
inline fun <S, T, A, B, C> PLens<S, T, A, B>.extracts(crossinline f: (A) -> C) =
        extract().ev().map { f(it) }.ev()

/**
 * Modify the value viewed through the lens and returns its *new* value
 */
fun <S, T, A, B> PLens<S, T, A, B>.mod(f: (A) -> B): IndexedState<S, T, B> = IndexedState { s ->
    val a = get(s)
    val b = f(a)
    set(s, b) toT b
}

/** modify the value viewed through the lens and returns its *old* value */
//def modo(f: A => B): IndexedState[S, T, A] =toState.leftMap(lens.modify(f))
fun <S, T, A, B> PLens<S, T, A, B>.modo(f: (A) -> B): IndexedState<S, T, A> = TODO()

fun <S, A> Lens<S, A>.mod_(f: (A) -> A) = State<S, Unit> {
    val a = get(it)
    val b = f(a)
    set(it, b) toT Unit
}

