---
layout: docs
title: Eq
permalink: /docs/typeclasses/eq/
---

## Eq

The `Eq` typeclass abstracts the ability to compare two instances of any object.
It can be considered the typeclass equivalent of Java's `Object#equals`.

Depending on your needs this comparison can be structural -the content of the object-, referential -the memory address of the object-, based on an identity -like an Id fields-, or any combination of the above.

```kotlin
import arrow.instances.*

// Enable the extension functions inside Eq using run
String.eq().run {
  "1".eqv("2")
}
// false
```

### Main Combinators

#### F.eqv

Compares two instances of `F` and returns true if they're considered equal for this instance.
It is the opposite comparison of `neqv`.

`fun F.eqv(b: F): Boolean`


```kotlin
Int.eq().run { 1.eqv(2) }
// false
```

#### neqv

Compares two instances of `F` and returns true if they're not considered equal for this instance.
It is the opposite comparison of `eqv`.

`fun neqv(a: F, b: F): Boolean`

```kotlin
Int.eq().run { 1.neqv(2) }
// true
```

### Laws

Arrow provides `EqLaws` in the form of test cases for internal verification of lawful instances and third party apps creating their own `Eq` instances.

#### Creating your own `Eq` instances

Eq provides one special instance that can be potentially applicable to most datatypes.
It uses kotlin's == comparison to compare any two instances.
Note that this instance will fail on many all datatypes that contain a property or field that doesn't implement structural equality, i.e. functions, typeclasses, non-data classes

```kotlin
import arrow.core.*
import arrow.typeclasses.*

// Option is a data class with a single value
Eq.any().run { Some(1).eqv(Option.just(1)) }
// true
```

```kotlin
// Fails because the wrapped function is not evaluated for comparison
Eq.any().run { Eval.later { 1 }.eqv(Eval.later { 1 }) }
// false
```

```kotlin
// using invoke constructor
val intEq = Eq<Int> { a, b -> a == b }
```

See [Deriving and creating custom typeclass]({{ '/docs/patterns/glossary' | relative_url }}) to provide your own `Eq` instances for custom datatypes.

### Data Types

Most of the datatypes in Arrow that are not related to functions provide instances of the `Eq` typeclass.

- [Id]({{ '/docs/datatypes/id/' | relative_url }})
- [Option]({{ '/docs/datatypes/option/' | relative_url }})
- [Either]({{ '/docs/datatypes/either/' | relative_url }})
- [Eval]({{ '/docs/datatypes/eval/' | relative_url }})
- `TupleN`
- [NonEmptyList]({{ '/docs/datatypes/nonemptylist/' | relative_url }})
- [Ior]({{ '/docs/datatypes/ior/' | relative_url }})
- [Const]({{ '/docs/datatypes/const/' | relative_url }})
- [Coproduct]({{ '/docs/datatypes/coproduct/' | relative_url }})
- [Try]({{ '/docs/datatypes/try/' | relative_url }})
- [Validated]({{ '/docs/datatypes/validated/' | relative_url }})
- [Free]({{ '/docs/free/free' | relative_url }})
- [FreeApplicative]({{ '/docs/free/freeapplicative' | relative_url }})
- [ListK]({{ '/docs/datatypes/listk/' | relative_url }})
- [SequenceK]({{ '/docs/datatypes/sequencek/' | relative_url }})
- [SetK]({{ '/docs/datatypes/setk/' | relative_url }})
- [MapK]({{ '/docs/datatypes/mapk/' | relative_url }})
- [SortedMapK]({{ '/docs/datatypes/sortedmapk/' | relative_url }})

Additionally all instances of [`Order`]({{ '/docs/typeclasses/order' | relative_url }}) and their MTL variants implement the `Eq` typeclass directly since they are all subtypes of `Eq`
