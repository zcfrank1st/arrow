package arrow.optics.dsl_poc

import arrow.core.Option
import arrow.data.ListKW
import arrow.data.ListKWHK
import arrow.data.MapKW
import arrow.optics.PTraversal
import arrow.optics.Setter
import arrow.optics.fromTraversable
import arrow.optics.listKWKindToListKW
import arrow.optics.nullableOptional
import arrow.optics.somePrism
import arrow.optics.typeclasses.Each
import arrow.optics.typeclasses.Index
import arrow.optics.typeclasses.each
import arrow.optics.typeclasses.index

interface BoundSetter<out S, A> {
    fun modify(f: (A) -> A): S
}

fun <T, S, A> BoundSetter<T, S>.setter(setter: Setter<S, A>) = object : BoundSetter<T, A> {
    override fun modify(f: (A) -> A): T = this@setter.modify { setter.modify(it, f) }
}

fun <T> T.setter() = object : BoundSetter<T, T> {
    override fun modify(f: (T) -> T) = f(this@setter)
}

fun <S, A> BoundSetter<S, A>.set(a: A) = modify { a }

//primitives
val <T, A> BoundSetter<T, A?>.nullable
    inline get() = setter(nullableOptional<A>().asSetter())

val <T, A> BoundSetter<T, Option<A>>.some
    inline get() = setter(somePrism<A>().asSetter())

fun <T, A> BoundSetter<T, ListKW<A>>.each(): BoundSetter<T, A> =
        setter(listKWKindToListKW<A>().reverse() compose PTraversal.fromTraversable<ListKWHK, A, A>().asSetter())

inline fun <T, reified S, reified A> BoundSetter<T, S>.each(EA: Each<S, A> = arrow.optics.typeclasses.each()): BoundSetter<T, A> =
        setter(EA.each().asSetter())

inline fun <T, reified A> BoundSetter<T, ListKW<A>>.get(i: Int): BoundSetter<T, A> =
        setter(index<ListKW<A>, Int, A>().index(i).asSetter())

inline fun <T, reified S, reified A> BoundSetter<T, S>.get(i: Int, ID: Index<S, Int, A> = index()): BoundSetter<T, A> =
        setter(ID.index(i).asSetter())

inline fun <T, reified S, reified I, reified A> BoundSetter<T, S>.get(i: I, ID: Index<S, I, A> = index(), dummy: Unit = Unit): BoundSetter<T, A> =
        setter(ID.index(i).asSetter())

fun main(args: Array<String>) {

    john.setter().company.nullable.address.street.name
            .modify(String::capitalize)
            .let(::println)

    employees.setter().employees.each().company.nullable.address.street.name
            .modify(String::toUpperCase)
            .let(::println)

    employees.setter().employees.get(0).company.nullable.address.street.name
            .modify(String::toUpperCase)
            .let(::println)

    db.setter().content.get<Db, MapKW<Keys, String>, Keys, String>(Three)
            .modify(String::reversed)
            .let(::println)

}
