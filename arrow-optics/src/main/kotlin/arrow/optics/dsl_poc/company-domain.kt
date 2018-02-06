package arrow.optics.dsl_poc

import arrow.data.ListKW
import arrow.data.k
import arrow.lenses

@lenses
data class Street(val number: Int, val name: String)

@lenses
data class Address(val city: String, val street: Street)

@lenses
data class Company(val name: String, val address: Address)

@lenses
data class Employee(val name: String, val company: Company?)

@lenses
data class CompanyEmployees(val employees: ListKW<Employee>)

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

val john = Employee("John Doe", Company("Kategory", Address("Functional city", Street(42, "lambda street"))))
val jane = Employee("Jane Doe", Company("Kategory", Address("Functional city", Street(42, "lambda street"))))

val employees = CompanyEmployees(listOf(john, jane).k())