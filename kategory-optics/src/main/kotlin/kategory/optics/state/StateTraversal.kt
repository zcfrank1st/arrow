package kategory.optics.state

import kategory.Id
import kategory.ListKW
import kategory.State
import kategory.functor
import kategory.optics.PTraversal
import kategory.optics.Traversal
import kategory.optics.modify
import kategory.toT

fun <S, T, A, B> PTraversal<S, T, A, B>.toState() = State<S, ListKW<A>> { s ->
    s toT getAll(s)
}

fun <S, T, A, B> PTraversal<S, T, A, B>.extract() = toState()

fun <S, T, A, B> PTraversal<S, T, A, B>.extracts(f: (ListKW<A>) -> B) =
        extract().map(f, Id.functor())

fun <S, A> Traversal<S, A>.mod(f: (A) -> A) = State<S, ListKW<A>> { s ->
    modify(s,f) toT getAll(s).map(f)
}

fun <S, A> Traversal<S, A>.mod_(f: (A) -> A) = State<S, Unit> { s ->
    modify(s,f) toT Unit
}
