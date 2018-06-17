---
layout: docs
title: Generic
permalink: /docs/generic/product/
---

## Arrow Generic

`arrow-generic` provides meta programming facilities over Product types like data classes, tuples, and heterogeneous lists; and Coproduct types like sealed classes.

### Install 

```groovy
compile 'io.arrow-kt:arrow-generic:$arrow_version'
```

### Features

#### @product

We refer to data classes, tuples, and heterogeneous lists as Product types because they all represent a container of typed values in which all those values need to be present.

That is to say that in the following data class both `balance` and `available` are properties of the `Account` class and both are typed and guaranteed to always be present within an `Account`

```kotlin
@product
data class Account(val balance: Int, val available: Int) {
  companion object
}
```

All `@product` annotated data classes must include a `companion object` so that codegen can be properly expanded as extension functions to the companion.

Because of such properties we can automatically derive interesting behaviors from our data classes by using the `@product` annotation:

#### Extensions

`@product` automatically derives instances for `Semigroup` and `Monoid` supporting recursion in declared data types. In the example below we are able to `+` two `Account` objects because the instance `Int.semigroup()` is provided by Arrow.

##### + operator

```kotlin
import arrow.core.*
import arrow.generic.*

Account(1000, 900) + Account(1000, 900)
// Account(balance=2000, available=1800)
```

##### combineAll

`@product` enables also syntax over `List<Account>` to reduce `(List<Account>) -> Account` automatically based also on the `Semigroup` instance, `@product` expects already defined instances for all contained data types which for most basic primitives Arrow already provides and for custom data types can be manually generated or automatically derived by Arrow with `@product`

```kotlin
listOf(Account(1000, 900), Account(1000, 900)).combineAll()
// Account(balance=2000, available=1800)
```

##### tupled

`@product` enables `Account#tupled` and `Tuple2#toAccount` extensions automatically to go back and forth between the data class values to tuple representations such as `Tuple2` with the same arity and property types as those declared in the data class for all data classes with at least 2 properties.

```kotlin
Account(1000, 900).tupled()
// Tuple2(a=1000, b=900)
```

```kotlin
Account(1000, 900).tupledLabeled()
// Tuple2(a=Tuple2(a=balance, b=1000), b=Tuple2(a=available, b=900))
```

```kotlin
Tuple2(1000, 900).toAccount()
// Account(balance=1000, available=900)
```

##### toHList

`@product` enables `Account#toHList` and `HList2#toAccount` extensions automatically to go back and forth between the data class value to a heterogeneous list representation such as `HList2` with the same arity and property types as those declared in the data class regardless of the number of properties.

```kotlin
Account(1000, 900).toHList()
// HCons(head=1000, tail=HCons(head=900, tail=arrow.generic.HNil@61038eb))
```

```kotlin
Account(1000, 900).toHListLabeled()
// HCons(head=Tuple2(a=balance, b=1000), tail=HCons(head=Tuple2(a=available, b=900), tail=arrow.generic.HNil@61038eb))
```

```kotlin
hListOf(1000, 900).toAccount()
// Account(balance=1000, available=900)
```

##### Applicative#mapTo___

`@product` allows us map independent values in the context of any `Applicative` capable data type straight to the data class inside the data type context

In the examples below we can observe how 2 different `Int` properties are returned inside a type constructor such as `Option`, `Try`, `Deferred` etc... and the automatically mapped to the shape of our `Account` data class removing all boilerplate from extracting the values from their context and returning an `Account` value in the same context.

```kotlin
import arrow.instances.*

val maybeBalance: Option<Int> = Option(1000)
val maybeAvailable: Option<Int> = Option(900)

ForOption extensions { 
  mapToAccount(maybeBalance, maybeAvailable)
}
// Some(Account(balance=1000, available=900))
```

```kotlin
val maybeBalance: Option<Int> = Option(1000)
val maybeAvailable: Option<Int> = None

ForOption extensions { 
  mapToAccount(maybeBalance, maybeAvailable) 
}
// None
```

```kotlin
val tryBalance: Try<Int> = Try { 1000 }
val tryAvailable: Try<Int> = Try { 900 }

ForTry extensions { 
  mapToAccount(tryBalance, tryAvailable)
}
// Success(value=Account(balance=1000, available=900))
```

```kotlin
val tryBalance: Try<Int> = Try { 1000 }
val tryAvailable: Try<Int> = Try { throw RuntimeException("BOOM") }

ForTry extensions { 
  mapToAccount(tryBalance, tryAvailable)
}
// Failure(exception=java.lang.RuntimeException: BOOM)
```

```kotlin
import arrow.effects.*
import kotlinx.coroutines.experimental.async

val asyncBalance: DeferredK<Int> = async { 1000 }.k()
val asyncAvailable: DeferredK<Int> = async { 900 }.k()

ForDeferredK extensions { 
  mapToAccount(asyncBalance, asyncAvailable)
}
// DeferredK(deferred=LazyDeferredCoroutine{New}@6af24db)
```

#### Typeclass instances

##### Semigroup 

Combine and reduce a data class based on it's internal properties reduction and combination properties as defined by their `Semigroup` instance.

```kotlin
with(Account.semigroup()) {
  Account(1000, 900).combine(Account(1000, 900))
}
// Account(balance=2000, available=1800)
```

##### Monoid 

Extends `Semigroup` by providing the concept of absent or empty value. It derives it's empty value based on the empty value of each one of it's contained properties.

```kotlin
emptyAccount()
// Account(balance=0, available=0)
```

```kotlin
Account.monoid().empty()
// Account(balance=0, available=0)
```

##### Eq 

Structural equality in terms of `Eq`, a type class that represents equality.

```kotlin
with(Account.eq()) {
  Account(1000, 900).eqv(Account(1000, 900))
}
// true
```

```kotlin
with(Account.eq()) {
  Account(1000, 900).neqv(Account(1000, 900))
}
// false
```

##### Show 

`toString` as a type class: `Show`

```kotlin
with(Account.show()) {
  Account(1000, 900).show()
}
// Account(balance=1000, available=900)
```

#### Creating instances for custom properties

Sometimes you may be in need of creating type class instances for custom properties that Arrow does not provide by default.

In the following example our `Car` data class contains a `maxSpeed: Speed` property for a custom type.

Arrow can auto derive `Semigroup`, `Monoid`, `Eq` and `Show` for `Car` as long as we also have instances for `Speed`.

```kotlin
data class Speed(val kmh: Int) {
  companion object
}

@product
data class Car(val mod: Int, val speed: Speed) {
  companion object
}
```

Once we attempt to compile this we would get an error similar to the one below:

```$xslt
:arrow-docs:compileKotlin: /home/raulraja/workspace/arrow/arrow/modules/docs/arrow-docs/build/generated/source/kaptKotlin/main/product.arrow.generic.car.kt: (60, 119): Unresolved reference.
```

This is because `Speed` is a data class not flagged as `@product`. Let's fix that:

```kotlin
@product
data class Speed(val kmh: Int) {
  companion object
}

@product
data class Car(val mod: Int, val speed: Speed) {
  companion object
}
```

The reason the code compiles now is that Arrow was able to complete the instance for `Car` once we proved we had one for `Speed`. 

Now that `Speed` is also flagged as `@product` its `Semigroup`, `Monoid`, `Show` and `Eq` instances are available and visible in `Car`

```kotlin
Speed(50) + Speed(50)
// Speed(kmh=100)
```

```kotlin
Car(Speed(50)) + Car(Speed(50))
// Car(speed=Speed(kmh=100))
```
