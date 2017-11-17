package kategory.optics

import kategory.*

/**
 * Transforms a [PLens] into [State].
 */
fun <S, T, A, B> PLens<S, T, A, B>.toState(): State<S, A> = State { s ->
    s toT get(s)
}

/**
 * Alias for [toState].
 */
fun <S, T, A, B> PLens<S, T, A, B>.extract(): State<S, A> = toState()

/**
 * Inspect the focus of a [PLens] and applies [f] over it in [State].
 *
 * @param f the function you want to apply to the focus.
 */
inline fun <S, T, A, B, C> PLens<S, T, A, B>.inspect(crossinline f: (A) -> C): State<S, C> = State { s ->
    s toT f(get(s))
}

/**
 * Modify the focus of a [PLens] and returns its new value.
 *
 * @param f the function you want to apply to the focus [A].
 */
fun <S, T, A, B> PLens<S, T, A, B>.mod(f: (A) -> B): IndexedState<S, T, B> = IndexedState { s ->
    val a = get(s)
    val b = f(a)
    set(s, b) toT b
}

/**
 * Modify the focus of a [PLens] and returns its old value.
 *
 * @param f the function you want to apply to the focus.
 */
fun <S, T, A, B> PLens<S, T, A, B>.modo(f: (A) -> B): IndexedState<S, T, A> = toState().imap(lift(f))

/**
 * Modify the focus of a [PLens].
 *
 * @param f the function you want to apply to the focus.
 */
fun <S, T, A, B> PLens<S, T, A, B>.mod_(f: (A) -> B): IndexedState<S, T, Unit> = IndexedState {
    modify(it, f) toT Unit
}

/**
 * Set a value [B] to the focus of a [PLens] and return that value.
 *
 * @param b the value to be set.
 */
fun <S, T, A, B> PLens<S, T, A, B>.assign(b: B): IndexedState<S, T, B> = mod { _ -> b }

/**
 * Set the value to the focus of a [PLens] and return the old focus.
 *
 * @param b the value to be set.
 */
fun <S, T, A, B> PLens<S, T, A, B>.assigno(b: B): IndexedState<S, T, A> = modo { _ -> b }

/**
 * Set the value to the focus of a [PLens].
 *
 * @param b the value to be set.
 */
fun <S, T, A, B> PLens<S, T, A, B>.assign_(b: B): IndexedState<S, T, Unit> = mod_ { _ -> b }

