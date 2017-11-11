package kategory.optics.state

import kategory.*
import kategory.optics.Optional
import kategory.optics.POptional
import kategory.optics.modify

/**
 * Transforms a POptional into a State
 */
fun <S, T, A, B> POptional<S, T, A, B>.toState() = State<S, Option<A>> { s ->
    s toT getOption(s)
}

/**
 * Extracts the value viewed through the optional
 */
fun <S, T, A, B> POptional<S, T, A, B>.extract(): StateT<IdHK, S, Option<A>> = toState()

/**
 * Extracts the value viewed through the optional and applies [f] over it
 */
inline fun <S, T, A, B> POptional<S, T, A, B>.extracts(crossinline f: (A) -> B): StateT<IdHK, S, Option<B>> =
        extract().ev().map { it.map(f) }.ev()

/**
 * Modify the value viewed through the Optional and return its *new* value, if there is one
 */
fun <S, T, A, B> POptional<S, T, A, B>.mod(f: (A) -> B): IndexedStateT<IdHK, S, T, Option<B>> =
        modo(f).map { it.map(f) }.ev()

/**
 * Modify the value viewed through the Optional and return its *old* value, if there was one
 */
fun <S, T, A, B> POptional<S, T, A, B>.modo(f: (A) -> B): IndexedStateT<IdHK, S, T, Option<A>> = IndexedStateT(Id.applicative()) { s ->
    Id.pure(modify(s, f) toT getOption(s))
}

/**
 * Modify the value viewed through the Optional and ignores both values
 */
fun <S, T, A, B> POptional<S, T, A, B>.mod_(f: (A) -> B): IndexedStateT<IdHK, S, T, Unit> = IndexedStateT(Id.applicative()) { s ->
    Id.pure(modify(s, f) toT Unit)
}

/**
 * Set the value viewed through the Optional and returns its *new* value
 */
fun <S, T, A, B> POptional<S, T, A, B>.assign(b: B): IndexedStateT<IdHK, S, T, Option<B>> = mod { _ -> b }

/**
 * Set the value viewed through the Optional and return its *old* value, if there was one
 */
fun <S, T, A, B> POptional<S, T, A, B>.assigno(b: B): IndexedStateT<IdHK, S, T, Option<A>> = modo { _ -> b }

/**
 * Set the value viewed through the Optional and ignores both values
 */
fun <S, T, A, B> POptional<S, T, A, B>.assign_(b: B): IndexedStateT<IdHK, S, T, Unit> = mod_{ _ -> b }