---
layout: docs
title: Semigroup
permalink: /docs/typeclasses/semigroup/
---

## Semigroup

A semigroup for some given type `A` has a single operation (which we will call `combine`), which takes two values of type `A`, and returns a value of type `A`. This operation must be guaranteed to be associative. That is to say that:

```kotlin
(a.combine(b)).combine(c)
```

must be the same as

```kotlin
a.combine(b.combine(c))
```

for all possible values of a, b ,c.

There are instances of `Semigroup` defined for many types found in Arrow and the Kotlin std lib.
For example, `Int` values are combined using addition by default but multiplication is also associative and forms another `Semigroup`.

### Examples

Now that you've learned about the Semigroup instance for Int try to guess how it works in the following examples:

```kotlin
import arrow.*
import arrow.typeclasses.*
import arrow.instances.*

ForInt extensions { 1.combine(2) }
// 3
```

```kotlin
import arrow.data.*

ListK.semigroup<Int>().run { listOf(1, 2, 3).k().combine(listOf(4, 5, 6).k()) }
// ListK(list=[1, 2, 3, 4, 5, 6])
```

```kotlin
import arrow.core.*

Option.monoid<Int>(Int.semigroup()).run { Option(1).combine(Option(2)) }
// Some(3)
```

```kotlin
Option.monoid<Int>(Int.semigroup()).run { Option(1).combine(None) }
// Some(1)
```

Many of these types have methods defined directly on them, which allow for such combining, e.g. `+` on `List`, but the value of having a `Semigroup` typeclass available is that these compose.

Additionaly `Semigroup` adds `+` syntax to all types for which a Semigroup instance exists:

```kotlin
Option.monoid<Int>(Int.semigroup()).run {
  Option(1) + Option(2)
}
// Some(3)
```

Contents partially adapted from [Scala Exercises Cat's Semigroup Tutorial](https://www.scala-exercises.org/cats/semigroup)


### Data Types

The following data types in Arrow provide instances that adhere to the `Semigroup` type class.

- [NonEmptyList]({{ '/docs/datatypes/nonemptylist' | relative_url }})
- [SequenceK]({{ '/docs/datatypes/sequencek' | relative_url }})
- [SetK]({{ '/docs/datatypes/setk' | relative_url }})
