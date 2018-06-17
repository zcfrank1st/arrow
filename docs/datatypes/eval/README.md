---
layout: docs
title: Eval
permalink: /docs/datatypes/eval/
video: FcaaTJhCEcw
---

## Eval

Eval is a monad that allows us to control evaluation and chain lazy operations that are not executed until `value()` is invoked.

Eval includes internally trampolining facilities which allows us to chain computations without fear of blowing up the stack.
There are two different factors that play into evaluation: memoization and laziness.

Eval supports memoization, eager and lazy evaluation strategies

### now

`Eval#now` creates an Eval instance from an already constructed value but still defers evaluation when chaining expressions with `map` and `flatMap`

```kotlin
import arrow.*
import arrow.core.*

val eager = Eval.now(1).map { it + 1 }
eager.value()
// 2
```

### later

`Eval#later` creates an Eval instance from a function deferring it's evaluation until `.value()` is invoked memoizing the computed value.

```kotlin
val lazyEvaled = Eval.later { "expensive computation" }
lazyEvaled.value()
// expensive computation
```

`"expensive computation"` is only computed once since the results are memoized and multiple calls to `value()` will just return the cached value.

### always

`Eval#always` creates an Eval instance from a function deferring it's evaluation until `.value()` is invoked recomputing each time `.value()` is invoked.

```kotlin
val alwaysEvaled = Eval.always { "expensive computation" }
alwaysEvaled.value()
// expensive computation
```

### Stack safety

`Eval` empowers stack safe programs by chaining lazy computations

```kotlin
fun even(n: Int): Eval<Boolean> =
  Eval.always { n == 0 }.flatMap {
    if(it == true) Eval.now(true)
    else odd(n - 1)
  }

fun odd(n: Int): Eval<Boolean> =
  Eval.always { n == 0 }.flatMap {
    if(it == true) Eval.now(false)
    else even(n - 1)
  }

// if not wrapped in eval this type of computation would blow the stack and result in a StackOverflowError
odd(100000).value()
// false
```

## Available Instances

* [Applicative]({{ '/docs/typeclasses/applicative' | relative_url }})
* [Bimonad]({{ '/docs/typeclasses/bimonad' | relative_url }})
* [Comonad]({{ '/docs/typeclasses/comonad' | relative_url }})
* [Functor]({{ '/docs/typeclasses/functor' | relative_url }})
* [Monad]({{ '/docs/typeclasses/monad' | relative_url }})

## Credits

Contents partially adapted from [Cats Eval](https://typelevel.org/cats/datatypes/eval.html)
