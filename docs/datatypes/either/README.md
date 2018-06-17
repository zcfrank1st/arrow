---
layout: docs
title: Either
permalink: /docs/datatypes/either/
video: q6HpChSq-xc
---

## Either

In day-to-day programming, it is fairly common to find ourselves writing functions that can fail.
For instance, querying a service may result in a connection issue, or some unexpected JSON response.

To communicate these errors it has become common practice to throw exceptions; however,
exceptions are not tracked in any way, shape, or form by the compiler. To see what
kind of exceptions (if any) a function may throw, we have to dig through the source code.
Then to handle these exceptions, we have to make sure we catch them at the call site. This
all becomes even more unwieldy when we try to compose exception-throwing procedures.

```kotlin
import arrow.*
import arrow.core.*

val throwsSomeStuff: (Int) -> Double = {x -> x.toDouble()}
val throwsOtherThings: (Double) -> String = {x -> x.toString()}
val moreThrowing: (String) -> List<String> = {x -> listOf(x)}
val magic = throwsSomeStuff.andThen(throwsOtherThings).andThen(moreThrowing)
magic
// (A) -> C
```

Assume we happily throw exceptions in our code. Looking at the types of the above functions, any of them could throw any number of exceptions -- we do not know. When we compose, exceptions from any of the constituent
functions can be thrown. Moreover, they may throw the same kind of exception
(e.g. `IllegalArgumentException`) and thus it gets tricky tracking exactly where an exception came from.

How then do we communicate an error? By making it explicit in the data type we return.

## Either vs Validated

In general, `Validated` is used to accumulate errors, while `Either` is used to short-circuit a computation
upon the first error. For more information, see the `Validated` vs `Either` section of the `Validated` documentation.

By convention the right hand side of an `Either` is used to hold successful values.

```kotlin
val right: Either<String, Int> = Either.Right(5)
right
// Right(b=5)
```

```kotlin
val left: Either<String, Int> = Either.Left("Something went wrong")
left
// Left(a=Something went wrong)
```
Because `Either` is right-biased, it is possible to define a Monad instance for it.

Since we only ever want the computation to continue in the case of `Right` (as captured by the right-bias nature),
we fix the left type parameter and leave the right one free.

So the map and flatMap methods are right-biased:

```kotlin
val right: Either<String, Int> = Either.Right(5)
right.flatMap{Either.Right(it + 1)}
// Right(b=6)
```

```kotlin
val left: Either<String, Int> = Either.Left("Something went wrong")
left.flatMap{Either.Right(it + 1)}
// Left(a=Something went wrong)
```

## Using Either instead of exceptions

As a running example, we will have a series of functions that will: 

* Parse a string into an integer
* Calculate the reciprocal
* Convert the reciprocal into a string

Using exception-throwing code, we could write something like this:

```kotlin
// Exception Style

fun parse(s: String): Int =
    if (s.matches(Regex("-?[0-9]+"))) s.toInt()
    else throw NumberFormatException("$s is not a valid integer.")

fun reciprocal(i: Int): Double =
    if (i == 0) throw IllegalArgumentException("Cannot take reciprocal of 0.")
    else 1.0 / i

fun stringify(d: Double): String = d.toString()
```

Instead, let's make the fact that some of our functions can fail explicit in the return type.

```kotlin
// Either Style

fun parse(s: String): Either<NumberFormatException, Int> =
    if (s.matches(Regex("-?[0-9]+"))) Either.Right(s.toInt())
    else Either.Left(NumberFormatException("$s is not a valid integer."))

fun reciprocal(i: Int): Either<IllegalArgumentException, Double> =
    if (i == 0) Either.Left(IllegalArgumentException("Cannot take reciprocal of 0."))
    else Either.Right(1.0 / i)

fun stringify(d: Double): String = d.toString()

fun magic(s: String): Either<Exception, String> =
    parse(s).flatMap{reciprocal(it)}.map{stringify(it)}

```

These calls to `parse` returns a `Left` and `Right` value

```kotlin
parse("Not a number")
// Left(a=java.lang.NumberFormatException: Not a number is not a valid integer.)
```

```kotlin
parse("2")
// Right(b=2)
```

Now, using combinators like `flatMap` and `map`, we can compose our functions together.

```kotlin
magic("0")
// Left(a=java.lang.IllegalArgumentException: Cannot take reciprocal of 0.)
```

```kotlin
magic("1")
// Right(b=1.0)
```

```kotlin
magic("Not a number")
// Left(a=java.lang.NumberFormatException: Not a number is not a valid integer.)
```

In the following exercise we pattern-match on every case the `Either` returned by `magic` can be in.
Note the `when` clause in the `Left` - the compiler will complain if we leave that out because it knows that
given the type `Either[Exception, String]`, there can be inhabitants of `Left` that are not
`NumberFormatException` or `IllegalArgumentException`. You should also notice that we are using
[SmartCast](https://kotlinlang.org/docs/reference/typecasts.html#smart-casts) for accessing to `Left` and `Right`
value.

```kotlin
val x = magic("2")
val value = when(x) {
    is Either.Left -> when (x.a){
        is NumberFormatException -> "Not a number!"
        is IllegalArgumentException -> "Can't take reciprocal of 0!"
        else -> "Unknown error"
    }
    is Either.Right -> "Got reciprocal: ${x.b}"
}
value
// Got reciprocal: 0.5
```

Instead of using exceptions as our error value, let's instead enumerate explicitly the things that
can go wrong in our program.

```kotlin
// Either with ADT Style

sealed class Error {
    object NotANumber : Error()
    object NoZeroReciprocal : Error()
}

fun parse(s: String): Either<Error, Int> =
        if (s.matches(Regex("-?[0-9]+"))) Either.Right(s.toInt())
        else Either.Left(Error.NotANumber)

fun reciprocal(i: Int): Either<Error, Double> =
        if (i == 0) Either.Left(Error.NoZeroReciprocal)
        else Either.Right(1.0 / i)

fun stringify(d: Double): String = d.toString()

fun magic(s: String): Either<Error, String> =
        parse(s).flatMap{reciprocal(it)}.map{stringify(it)}
```

For our little module, we enumerate any and all errors that can occur. Then, instead of using
exception classes as error values, we use one of the enumerated cases. Now when we pattern match,
we are able to comphrensively handle failure without resulting to an `else` branch; moreover
since Error is sealed, no outside code can add additional subtypes which we might fail to handle.

```kotlin
val x = magic("2")
when(x) {
    is Either.Left -> when (x.a){
        is Error.NotANumber -> "Not a number!"
        is Error.NoZeroReciprocal -> "Can't take reciprocal of 0!"
    }
    is Either.Right -> "Got reciprocal: ${x.b}"
}
```

## Syntax

Either can also map over the `left` value with `mapLeft` which is similar to map but applies on left instances.

```kotlin
val r : Either<Int, Int> = Either.Right(7)
r.mapLeft {it + 1}
val l: Either<Int, Int> = Either.Left(7)
l.mapLeft {it + 1}
// Left(a=8)
```

`Either<A, B>` can be transformed to `Either<B,A>` using the `swap()` method.

```kotlin
val r: Either<String, Int> = Either.Right(7)
r.swap()
// Left(a=7)
```

For using Either's syntax on arbitrary data types.
This will make possible to use the `left()`, `right()`, `contains()`, `getOrElse()` and `getOrHandle()` methods:

```kotlin
7.right()
// Right(b=7)
```

```kotlin
"hello".left()
// Left(a=hello)
```

```kotlin
val x = 7.right()
x.contains(7)
// true
```

```kotlin
val x = "hello".left()
x.getOrElse { 7 }
// 7
```

```kotlin
val x = "hello".left()
x.getOrHandle { "$it world!" }
// hello world!
```

For creating Either instance based on a predicate, use `Either.cond()` method :

```kotlin
Either.cond(true, { 42 }, { "Error" })
// Right(b=42)
```

```kotlin
Either.cond(false, { 42 }, { "Error" })
// Left(a=Error)
```

Another operation is `fold`. This operation will extract the value from the Either, or provide a default if the value is `Left`

```kotlin
val x : Either<Int, Int> = 7.right()
x.fold({ 1 }, { it + 3 }) 
// 10
```

```kotlin
val y : Either<Int, Int> = 7.left()
y.fold({ 1 }, { it + 3 }) 
// 1
```

The `getOrHandle()` operation allows the transformation of an `Either.Left` value to a `Either.Right` using
the value of `Left`. This can be useful when a mapping to a single result type is required like `fold()` but without
the need to handle `Either.Right` case.

As an example we want to map an `Either<Throwable, Int>` to a proper HTTP status code:

```kotlin
val r: Either<Throwable, Int> = Either.Left(NumberFormatException())
val httpStatusCode = r.getOrHandle {
	when(it) {
		is NumberFormatException -> 400
		else -> 500
	}
} // 400
```

 Arrow contains `Either` instances for many useful typeclasses that allows you to use and transform right values.
 Both Option and Try don't require a type parameter with the following functions, but it is specifically used for Either.Left

 [`Functor`]({{ '/docs/typeclasses/functor/' | relative_url }})

 Transforming the inner contents

```kotlin
import arrow.instances.*
 
ForEither<Int>() extensions { 
   Right(1).map {it + 1}
}
// Right(b=2)
```

 [`Applicative`]({{ '/docs/typeclasses/applicative/' | relative_url }})

 Computing over independent values

```kotlin
ForEither<Int>() extensions { 
  tupled(Either.Right(1), Either.Right("a"), Either.Right(2.0))
}
// Right(b=Tuple3(a=1, b=a, c=2.0))
```

 [`Monad`]({{ '/docs/typeclasses/monad/' | relative_url }})

 Computing over dependent values ignoring absence

```kotlin
ForEither<Int>() extensions {
 binding {
    val a = Either.Right(1).bind()
    val b = Either.Right(1 + a).bind()
    val c = Either.Right(1 + b).bind()
    a + b + c
 }
}
// Right(6)
```

## Available Instances

* [Show]({{ '/docs/typeclasses/show' | relative_url }})
* [Eq]({{ '/docs/typeclasses/eq' | relative_url }})
* [Applicative]({{ '/docs/typeclasses/applicative' | relative_url }})
* [ApplicativeError]({{ '/docs/typeclasses/applicativeerror' | relative_url }})
* [Foldable]({{ '/docs/typeclasses/foldable' | relative_url }})
* [Functor]({{ '/docs/typeclasses/functor' | relative_url }})
* [Monad]({{ '/docs/typeclasses/monad' | relative_url }})
* [MonadError]({{ '/docs/typeclasses/monaderror' | relative_url }})
* [SemigroupK]({{ '/docs/typeclasses/semigroupk' | relative_url }})
* [Traverse]({{ '/docs/typeclasses/traverse' | relative_url }})
* [TraverseFilter]({{ '/docs/typeclasses/traversefilter' | relative_url }})
* [Each]({{ '/docs/optics/each' | relative_url }})
