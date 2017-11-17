package kategory.optics

import kategory.*

/**
 * Transforms a [POptional] into a [State].
 */
fun <S, T, A, B> POptional<S, T, A, B>.toState() = State<S, Option<A>> { s ->
    s toT getOption(s)
}

/**
 * Alias for [toState].
 */
fun <S, T, A, B> POptional<S, T, A, B>.extract(): State<S, Option<A>> = toState()

/**
 * Inspect the focus of a [POptional] and applies [f] over it in [State].
 *
 * @param f the function you want to apply to the focus.
 */
inline fun <S, T, A, B, C> POptional<S, T, A, B>.inspect(crossinline f: (A) -> C): State<S, Option<C>> =
        extract().ev().map { it.map(f) }.ev()

/**
 * Modify the focus of a [POptional] and returns its new value, if there is one.
 *
 * @param f the function you want to apply to the focus [A].
 */
fun <S, T, A, B> POptional<S, T, A, B>.mod(f: (A) -> B): IndexedState<S, T, Option<B>> = modo(f).map {
    it.map(f)
}.ev()

/**
 * Modify the focus of a [POptional] and returns its old value, if there was one.
 *
 * @param f the function you want to apply to the focus.
 */
fun <S, T, A, B> POptional<S, T, A, B>.modo(f: (A) -> B): IndexedState<S, T, Option<A>> = IndexedState { s ->
    modify(s, f) toT getOption(s)
}

/**
 * Modify the focus of a [POptional].
 */
fun <S, T, A, B> POptional<S, T, A, B>.mod_(f: (A) -> B): IndexedState<S, T, Unit> = IndexedState { s ->
    modify(s, f) toT Unit
}

/**
 * Set a value [B] to the focus of a [POptional] and return that value, if there is one.
 *
 * @param b the value to be set.
 */
fun <S, T, A, B> POptional<S, T, A, B>.assign(b: B): IndexedState<S, T, Option<B>> = mod { _ -> b }

/**
 * Set the value to the focus of a [POptional] and return the old focus, if there was one.
 *
 * @param b the value to be set.
 */
fun <S, T, A, B> POptional<S, T, A, B>.assigno(b: B): IndexedState<S, T, Option<A>> = modo { _ -> b }

/**
 * Set the value to the focus of a [POptional].
 *
 * @param b the value to be set.
 */
fun <S, T, A, B> POptional<S, T, A, B>.assign_(b: B): IndexedState<S, T, Unit> = mod_ { _ -> b }