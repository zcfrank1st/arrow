package kategory.optics

import kategory.*

/**
 * Transforms a [PTraversal] into a [State].
 */
fun <S, T, A, B> PTraversal<S, T, A, B>.toState() = State<S, ListKW<A>> { s ->
    s toT getAll(s)
}

/**
 * Alias for [toState].
 */
fun <S, T, A, B> PTraversal<S, T, A, B>.extract() = toState()

/**
 * Inspect the foci of a [PTraversal] and applies [f] to it in [State].
 *
 * @param f the function you want to apply to the foci.
 */
fun <S, T, A, B, C> PTraversal<S, T, A, B>.inspect(f: (ListKW<A>) -> C): State<S, C> = extract().map(f, Id.monad())

/**
 * Modify the foci of a [PTraversal] and returns its new values.
 *
 * @param f the function you want to apply to a focus [A].
 */
fun <S, T, A, B> PTraversal<S, T, A, B>.mod(f: (A) -> B): IndexedState<S, T, ListKW<B>> =IndexedState { s ->
    val aas = getAll(s)
    modify(s, f) toT aas.map(f)
}

/**
 * Modify the foci of a [PTraversal] and returns its old values.
 *
 * @param f the function you want to apply to a focus [A].
 */
fun <S, T, A, B> PTraversal<S, T, A, B>.modo(f: (A) -> B): IndexedState<S, T, ListKW<A>> = toState().imap(Id.functor(), lift(f))

/**
 * Modify the foci of a [PTraversal].
 *
 * @param f the function you want to apply to a focus [A].
 */
fun <S, T, A, B> PTraversal<S, T, A, B>.mod_(f: (A) -> B): IndexedState<S, T, Unit> = IndexedState { s ->
    modify(s, f) toT Unit
}

/**
 * Set a foci [B] to the focus of a [PLens] and return that value.
 *
 * @param b the value to be set.
 */
fun <S, T, A, B> PTraversal<S, T, A, B>.assign(b: B): IndexedState<S, T, ListKW<B>> = mod { _ -> b }

/**
 * Set the value to the foci of a [PTraversal] and return the old focus.
 *
 * @param b the value to be set.
 */
fun <S, T, A, B> PTraversal<S, T, A, B>.assigno(b: B): IndexedState<S, T, ListKW<A>> = modo { _ -> b }

/**
 * Set the value to the foci of a [PTraversal].
 *
 * @param b the value to be set.
 */
fun <S, T, A, B> PTraversal<S, T, A, B>.assign_(b: B): IndexedState<S, T, Unit> = mod_ { _ -> b }