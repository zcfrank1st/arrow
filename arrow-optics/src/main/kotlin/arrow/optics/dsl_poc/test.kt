package arrow.optics.dsl_poc

import arrow.core.Option
import arrow.data.ListKW
import arrow.data.ListKWHK
import arrow.optics.PTraversal
import arrow.optics.Setter
import arrow.optics.listKWKindToListKW
import arrow.optics.nullableOptional
import arrow.optics.somePrism

interface BoundSetter<S, A> {
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
val <T, A> BoundSetter<T, A?>.nullable get() = setter(nullableOptional<A>().asSetter())
val <T, A> BoundSetter<T, Option<A>>.some get() = setter(somePrism<A>().asSetter())
val <T, A> BoundSetter<T, ListKW<A>>.each: BoundSetter<T, A>
    get() = setter(listKWKindToListKW<A>().reverse() compose PTraversal.fromTraversable<ListKWHK, A, A>().asSetter())

//Generated
val <T> BoundSetter<T, Employee>.name
    @JvmName("employeeName")
    inline get() = setter(employeeName().asSetter())
val <T> BoundSetter<T, Employee>.company
    inline get() = setter(employeeCompany().asSetter())
val <T> BoundSetter<T, Company>.name
    @JvmName("companyName")
    inline get() = setter(companyName().asSetter())
val <T> BoundSetter<T, Company>.address
    inline get() = setter(companyAddress().asSetter())
val <T> BoundSetter<T, Address>.city
    inline get() = setter(addressCity().asSetter())
val <T> BoundSetter<T, Address>.street
    inline get() = setter(addressStreet().asSetter())
val <T> BoundSetter<T, Street>.number
    inline get() = setter(streetNumber().asSetter())
val <T> BoundSetter<T, Street>.name
    inline get() = setter(streetName().asSetter())
val <T> BoundSetter<T, CompanyEmployees>.employees
    inline get() = setter(companyEmployeesEmployees().asSetter())

fun main(args: Array<String>) {

    john.setter().company.nullable.address.street.name
            .modify(String::capitalize)
            .let(::println)

    employees.setter().employees.each.company.nullable.address.street.name
            .modify(String::toUpperCase)
            .let(::println)

}
