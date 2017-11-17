package kategory.optics

import kategory.*

/**
 * Modify the focus of a [PSetter].
 *
 * @param f the function you want to apply to the focus.
 */
fun <S, T, A, B> PSetter<S, T, A, B>.mod_(f: (A) -> B): IndexedState<S, T, Unit> = IndexedState { s ->
    modify(s, f) toT Unit
}

/**
 * Set the value to the focus of a [PSetter].
 *
 * @param b the value to be set.
 */
fun <S, T, A, B> PSetter<S, T, A, B>.assign_(b: B): IndexedState<S, T, Unit> = mod_ { _ -> b }