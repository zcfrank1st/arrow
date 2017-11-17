package kategory.optics

import kategory.*

/**
 * Transforms a [Getter] into a [State].
 */
fun <S, A> Getter<S, A>.toState(): State<S, A> = State { s ->
    s toT get(s)
}

/**
 * Alias for [toState]
 */
fun <S, A> Getter<S, A>.extract(): State<S, A> = State { s ->
    s toT get(s)
}

/**
 * Inspect the focus of a [Getter] and applies [f] over it in [State].
 *
 * @param f the function you want to apply to the focus.
 */
inline fun <S, A, B> Getter<S, A>.inspect(crossinline f: (A) -> B): State<S, B> = State { s ->
    s toT f(get(s))
}
