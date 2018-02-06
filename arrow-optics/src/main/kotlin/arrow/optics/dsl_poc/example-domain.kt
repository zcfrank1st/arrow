package arrow.optics.dsl_poc

import arrow.bounded
import arrow.data.ListKW
import arrow.data.k
import arrow.lenses

@bounded
@lenses
data class Street(val number: Int, val name: String)

@bounded
@lenses
data class Address(val city: String, val street: Street)

@bounded
@lenses
data class Company(val name: String, val address: Address)

@bounded
@lenses
data class Employee(val name: String, val company: Company?)

@bounded
@lenses
data class CompanyEmployees(val employees: ListKW<Employee>)

val john = Employee("John Doe", Company("Kategory", Address("Functional city", Street(42, "lambda street"))))
val jane = Employee("Jane Doe", Company("Kategory", Address("Functional city", Street(42, "lambda street"))))

val employees = CompanyEmployees(listOf(john, jane).k())