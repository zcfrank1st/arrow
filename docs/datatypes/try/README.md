---
layout: docs
title: Try
permalink: /docs/datatypes/try/
video: XavztYVMUqI
---

## Try

Arrow has [lots of different types of error handling and reporting](http://arrow-kt.io/docs/patterns/error_handling/), which allows you to choose the best strategy for your situation.

For example, we have `Option` to model the absence of a value, or `Either` to model the return of a function as a type that may have been successful, or may have failed.

On the other hand, we have `Try`, which represents a computation that can result in an `A` result (as long as the computation is successful) or in an exception if something has gone wrong.

That is, there are only two possible implementations of `Try`: a `Try` instance where the operation has been successful, which is represented as `Success<A>`; or a `Try` instance where the computation has failed with a `Throwable`, which is represented as `Failure<A>`.

With just this explanation you might think that we are talking about an `Either<Throwable, A>`, and you are not wrong. `Try` can be implemented in terms of `Either`, but its use cases are very different.

If we know that an operation could result in a failure, for example, because it is code from a library over which we have no control, or better yet, some method from the language itself. We can use `Try` as a substitute for the well-known `try-catch`, allowing us to rise to all its goodness.

The following example represents the typical case when consuming Java code, where domain errors are represented with exceptions.  

```kotlin
open class GeneralException: Exception()

class NoConnectionException: GeneralException()

class AuthorizationException: GeneralException()

fun checkPermissions() {
    throw AuthorizationException()
}

fun getLotteryNumbersFromCloud(): List<String> {
    throw NoConnectionException()
}

fun getLotteryNumbers(): List<String> {
    checkPermissions()

    return getLotteryNumbersFromCloud()
}
```

The traditional way to control this would be to use a `try-catch` block, as we have said before:

```kotlin
try {
    getLotteryNumbers()
} catch (e: NoConnectionException) {
    //...
} catch (e: AuthorizationException) {
    //...
}
```

However, we could use `Try` to retrieve the computation result in a much cleaner way:

```kotlin
import arrow.*
import arrow.core.*

val lotteryTry = Try { getLotteryNumbers() }
lotteryTry
// Failure(exception=Line_1$AuthorizationException)
```

By using `getOrDefault` we can give a default value to return, when the computation fails, similar to what we can also do with `Option` when there is no value:

```kotlin
lotteryTry.getOrDefault { emptyList() }
// []
```

If the underlying failure is useful to determine the default value, `getOrElse` can be used:

```kotlin
lotteryTry.getOrElse { ex: Throwable -> emptyList() }
// []
```

`getOrElse` can generally be used anywhere `getOrDefault` is used, ignoring the exception if it's not needed:

```kotlin
lotteryTry.getOrElse { emptyList() }
// []
```

If you want to perform a check on a possible success, you can use `filter` to convert successful computations in failures if conditions aren't met:

```kotlin
lotteryTry.filter {
    it.size < 4
}
// Failure(exception=Line_1$AuthorizationException)
```

We can also use `recover` which allow us to recover from a particular error (we receive the error and have to return a new value):

```kotlin
lotteryTry.recover { exception ->
    emptyList()
}
// Success(value=[])
```

Or if you have another different computation that can also fail, you can use `recoverWith` to recover from an error (as you do with `recover`, but in this case, returning a new `Try`):

```kotlin
enum class Source {
    CACHE, NETWORK
}

fun getLotteryNumbers(source: Source): List<String> {
    checkPermissions()

    return getLotteryNumbersFromCloud()
}

Try { getLotteryNumbers(Source.NETWORK) }.recoverWith {
    Try { getLotteryNumbers(Source.CACHE) }
}
// Failure(exception=Line_1$AuthorizationException)
```

When you want to handle both cases of the computation you can use `fold`. With `fold` we provide two functions, one for transforming a failure into a new value, the second one to transform the success value into a new one:

```kotlin
lotteryTry.fold(
    { emptyList<String>() },
    { it.filter { it.toIntOrNull() != null } })
// []
```

Or, as we have with `recoverWith`, we can use a version of `fold` which allows us to handle both cases with functions that return a new instance of `Try`, `transform`:

```kotlin
lotteryTry.transform(
    { Try { it.map { it.toInt() } } },
    { Try.just(emptyList<Int>()) })
// Success(value=[])
```

Lastly, Arrow contains `Try` instances for many useful typeclasses that allows you to use and transform fallibale values:

[`Functor`]({{ '/docs/typeclasses/functor/' | relative_url }})

Transforming the value, if the computation is a success:

```kotlin
import arrow.typeclasses.*
import arrow.instances.*

ForTry extensions {
  Try { "3".toInt() }.map { it + 1} 
}
// Success(value=4)
```

[`Applicative`]({{ '/docs/typeclasses/applicative/' | relative_url }})

Computing over independent values:

```kotlin
ForTry extensions {
  tupled(Try { "3".toInt() }, Try { "5".toInt() }, Try { "nope".toInt() })
}
// Failure(exception=java.lang.NumberFormatException: For input string: "nope")
```

[`Monad`]({{ '/docs/typeclasses/monad/' | relative_url }})

Computing over dependent values ignoring failure:

```kotlin
ForTry extensions {
  binding {
    val a = Try { "3".toInt() }.bind()
    val b = Try { "4".toInt() }.bind()
    val c = Try { "5".toInt() }.bind()
    a + b + c
  }
} // Success(value=12)
```

```kotlin
ForTry extensions {
  binding {
    val a = Try { "none".toInt() }.bind()
    val b = Try { "4".toInt() }.bind()
    val c = Try { "5".toInt() }.bind()

    a + b + c
  } 
}
// Failure(exception=java.lang.NumberFormatException: For input string: "none")
```

Computing over dependent values that are automatically lifted to the context of `Try`:

```kotlin
ForTry extensions { 
  bindingCatch {
    val a = "none".toInt()
    val b = "4".toInt()
    val c = "5".toInt()
    a + b + c
  } 
}
// Failure(exception=java.lang.NumberFormatException: For input string: "none")
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
* [Traverse]({{ '/docs/typeclasses/traverse' | relative_url }})
* [Each]({{ '/docs/optics/each' | relative_url }})
