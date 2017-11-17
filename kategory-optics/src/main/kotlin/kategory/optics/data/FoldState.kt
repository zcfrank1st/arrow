package kategory.optics

import kategory.*
/**
 * Transforms a [Fold] into a [State].
 */
fun <S, A> Fold<S, A>.toState(): State<S, ListKW<A>> = State { s ->
    s toT getAll(s)
}

/**
 * Alias for [toState]
 */
fun <S, A> Fold<S, A>.extract(): State<S, ListKW<A>> = State { s ->
    s toT getAll(s)
}

/**
 * Inspect the foci of a [S] and applies [f] to it in [State].
 *
 * @param f the function you want to apply to the foci.
 */
fun <S, A, B> Fold<S, A>.inspect(f: (ListKW<A>) -> B): State<S, B> = extract().map(f)
