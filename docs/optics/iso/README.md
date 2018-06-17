---
layout: docs
title: Iso
permalink: /docs/optics/iso/
---

## Iso

An `Iso` is a loss less invertible optic that defines an isomorphism between a type `S` and `A` i.e. a data class and its properties represented by `TupleN`.

Isos can be seen as a pair of functions that represent an isomorphism, `get` and `reverseGet`. So an `Iso<S, A>` represents two getters: `get: (S) -> A` and `reverseGet: (A) -> S` where `S` is called the source of the `Iso` and `A` is called the focus or target of the `Iso`.

A simple structure `Point2D` is equivalent to `Tuple2<Int, Int>` so we can create an `Iso<Point2D, Tuple2<Int, Int>>`

```kotlin
import arrow.*
import arrow.core.*
import arrow.optics.*

data class Point2D(val x: Int, val y: Int)

val pointIsoTuple: Iso<Point2D, Tuple2<Int, Int>> = Iso(
    get = { point -> point.x toT point.y },
    reverseGet = { tuple -> Point2D(tuple.a, tuple.b) }
)

val point = Point2D(6, 10)
point
// Point2D(x=6, y=10)
```
```kotlin
val tuple = pointIsoTuple.get(point)
tuple
// Tuple2(a=6, b=10)
```
```kotlin
pointIsoTuple.reverseGet(tuple)
// Point2D(x=6, y=10)
```

Given an `Iso<Point2D, Tuple2<Int, Int>>` we also have an `Iso<Tuple2<Int, Int>, Point2D>` since it represents an isomorphism between equivalent structures we can reverse it.

```kotlin
val reversedIso: Iso<Tuple2<Int, Int>, Point2D> = pointIsoTuple.reverse()
```

Using an `Iso` we can modify our source `S` with a function that works on our focus `A`.

```kotlin
val addFive: (Tuple2<Int, Int>) -> Tuple2<Int, Int> = { tuple2 -> (tuple2.a + 5) toT (tuple2.b + 5) }
pointIsoTuple.modify(point, addFive)
// Point2D(x=11, y=15)
```

A function `(A) -> A` can be lifted to a function `(S) -> S`

```kotlin
val liftedAddFive: (Point2D) -> Point2D = pointIsoTuple.lift(addFive)
liftedAddFive(point)
// Point2D(x=11, y=15)
```

We can do the same with a Functor mapping

```kotlin
import arrow.data.*

pointIsoTuple.modifyF(Try.functor(), point) {
    Try { (tuple.a / 2) toT (tuple.b / 2) }
}
// Success(value=Point2D(x=3, y=5))
```

```kotlin
val liftF: (Point2D) -> TryOf<Point2D> = pointIsoTuple.liftF(Try.functor()) {
    Try { (tuple.a / 2) toT (tuple.b / 0) }
}

liftF(point)
// Failure(exception=java.lang.ArithmeticException: / by zero)
```

### Composition

By composing isos we can create additional isos without defining them. When dealing with different APIs or frameworks we frequently run into multiple equivalent but different structures like `Point2D`, `Tuple2`, `Pair`, `Coord`, etc.

```kotlin
data class Coord(val xAxis: Int, val yAxis: Int)

val pairIsoCoord: Iso<Pair<Int, Int>, Coord> = Iso(
        get = { pair -> Coord(pair.first, pair.second) },
        reverseGet = { coord -> coord.xAxis to coord.yAxis }
)

val tupleIsoPair: Iso<Tuple2<Int, Int>, Pair<Int, Int>> = Iso(
        get = { tuple -> tuple.a to tuple.b },
        reverseGet = { pair -> pair.first toT pair.second }
)
```

By composing `pointIsoTuple`, `pairIsoCoord` and `tupleIsoPair` (and/or reversing) we can use `Point2D`, `Tuple2<Int, Int>`, `Pair<Int, Int>` and `Coord` interchangeably as we can lift functions to the required structure.

Composing an `Iso` with functions can also be useful to change input or output type of a function. In the `Iso<A?, Option<A>>` is available in `arrow-optics` as `nullableToOption()`.

```kotlin
val unknownCode: (String) -> String? = { value ->
    "unkown $value"
}

val nullableOptionIso: Iso<String?, Option<String>> = nullableToOption()
(unknownCode andThen nullableOptionIso::get)("Retrieve an Option")
```

`Iso` can be composed with all optics and composition them results in the following optics.

|   | Iso | Lens | Prism |Optional | Getter | Setter | Fold | Traversal |
| --- | --- | --- | --- |--- | --- | --- | --- | --- |
| Iso | Iso | Lens | Prism | Optional | Getter | Setter | Fold | Traversal |

### Generating isos

To avoid boilerplate, isos can be generated for a `data class` to `TupleN` with 2 to 10 parameters by the `@optics` annotation.
The `Iso` will be generated as a extension property on the companion object `val T.Companion.iso`.

```kotlin
@optics data class Pos(val x: Int, val y: Int) {
  companion object
}
```
```kotlin
val iso: Iso<Pos, Tuple2<Int, Int>> = Pos.iso
```

### Polymorphic isos
When dealing with polymorphic equivalent structures we can create polymorphic isos to allow us to morph the type of the focus (and as a result the constructed type) of our `PIso`.

Given our previous structures `Tuple2<A, B>` and `Pair<A, B>` we can create a polymorphic `PIso` that represents a `get: (Tuple2<A, B>) -> Pair<A, B>` and a `reverseGet: (Tuple2<C, D) -> Pair<C, D>`.

```kotlin
fun <A, B, C, D> tuple2(): PIso<Tuple2<A, B>, Pair<C, D>, Pair<A, B>, Tuple2<C, D>> = PIso(
        { tuple -> tuple.a to tuple.b },
        { tuple -> tuple.a to tuple.b }
)
```

Above defined `PIso` can lift a `reverse` function of `(Pair<A, B>) -> Tuple2<B, A>` to a function `(Tuple2<A, B>) -> Pair<B, A>`.

```kotlin
val reverseTupleAsPair: (Tuple2<Int, String>) -> Pair<String, Int> =
        tuple2<Int, String, String, Int>().lift { intStringPair -> intStringPair.second toT intStringPair.first }
val reverse: Pair<String, Int> = reverseTupleAsPair(5 toT "five")
reverse
//(five, 5)
```

### Laws

Arrow provides [`IsoLaws`][iso_laws_source]{:target="_blank"} in the form of test cases for internal verification of lawful instances and third party apps creating their own isos.

[iso_laws_source]: https://github.com/arrow-kt/arrow/blob/master/modules/core/arrow-test/src/main/kotlin/arrow/test/laws/IsoLaws.kt