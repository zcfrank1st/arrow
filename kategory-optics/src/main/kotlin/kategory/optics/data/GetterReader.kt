package kategory.optics

import kategory.*

/**
 * Transforms a [Getter] into a [Reader].
 */
fun <S, A> Getter<S, A>.toReader(): ReaderT<IdHK, S, A> = Reader(this@toReader::get)

/**
 * Ask the focus of the [Getter] as a [Reader].
 */
fun <S, A> Getter<S, A>.ask(): ReaderT<IdHK, S, A> = toReader()

/**
 * Asks the focus of the [Getter] applied over [f] as a [Reader].
 *
 * @param f the function you want to apply to the focus.
 */
fun <S, A, B> Getter<S, A>.asks(f: (A) -> B): ReaderT<IdHK, S, B> = toReader().map(f)