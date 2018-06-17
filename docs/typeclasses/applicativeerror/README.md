---
layout: docs
title: ApplicativeError
permalink: /docs/typeclasses/applicativeerror/
---

## ApplicativeError

ApplicativeError is the typeclase used to explicitly represent errors during independent computations.
It is parametrized to an error type `E`, which means the datatype has at least a "success" and a "failure" version.

These errors can come in the form of `Throwable`, `Exception`, or any other type that is more relevant to the domain;
like for example a sealed class UserNotFoundReason that contains 3 inheritors.

Some of the datatypes Λrrow provides can have these error types already fixed.
That's the case of [`Try<A>`]({{ '/docs/datatypes/try' | relative_url }}), which has its error type fixed to `Throwable`.
Other datatypes like [`Either<E, A>`]({{ '/docs/datatypes/either' | relative_url }}) allow for the user to apply their error type of choice.

### Main Combinators

`ApplicativeError` inherits all the combinators available in [`Applicative`]({{ '/docs/typeclasses/applicative' | relative_url }}). It also adds several of its own.

#### raiseError

A constructor function. It lifts an exception into the computational context of a type constructor.

```kotlin
import arrow.*
import arrow.core.*

Either.applicativeError<Throwable>().raiseError<Int>(RuntimeException("Paco"))
// Left(a=java.lang.RuntimeException: Paco)
```

```kotlin
import arrow.data.*

Try.applicativeError().raiseError<Int>(RuntimeException("Paco"))
// Failure(exception=java.lang.RuntimeException: Paco)
```

```kotlin
import arrow.effects.*

IO.applicativeError().raiseError<Int>(RuntimeException("Paco"))
// RaiseError(exception=java.lang.RuntimeException: Paco)
```

#### Kind<F, A>#handleErrorWith

This method requires a function that creates a new datatype from an error, `(E) -> Kind<F, A>`. This function is used as a catch + recover clause for the current instance, allowing it to return a new computation after a failure.

If [`Monad`]({{ '/docs/typeclasses/monad' | relative_url }}) has `flatMap` to allow mapping the value inside a *successful* datatype into a new datatype, you can think of `handleErrorWith` as a way that allows you to map the value of a *failed datatype into a new datatype.

```kotlin
val AE_EITHER = Either.applicativeError<Throwable>()

val success: Either<Throwable, Int> = Either.Right(1)

AE_EITHER.run { success.handleErrorWith { t -> Either.Right(0) } }
// Right(b=1)
```

```kotlin
val failure: Either<Throwable, Int> = Either.Left(RuntimeException("Boom!"))

AE_EITHER.run { failure.handleErrorWith { t -> Either.Right(0) } }
// Right(b=0)
```

#### Kind<F, A>#handleError

Similar to `handleErrorWith`, except the function can return any regular value. This value will be wrapped and used as a return.

```kotlin
AE_EITHER.run { success.handleError { t -> 0 } }
// Right(b=1)
```

```kotlin
AE_EITHER.run { failure.handleError { t -> 0 } }
// Right(b=0)
```

#### Kind<F, A>#attempt

Maps the current content of the datatype to an [`Either<E, A>`]({{ '/docs/datatypes/either' | relative_url }}), recovering from any previous error state.

```kotlin
val AE_TRY = Try.applicativeError()
```

```kotlin
AE_TRY.run { Try { "3".toInt() }.attempt() }
// Success(value=Right(b=3))
```

```kotlin
AE_TRY.run { Try { "nope".toInt() }.attempt() }
// Success(value=Left(a=java.lang.NumberFormatException: For input string: "nope"))
```

#### fromEither

Constructor function from an [`Either<E, A>`]({{ '/docs/datatypes/either' | relative_url }}) to the current datatype.

```kotlin
AE_TRY.fromEither(Either.Right(1))
// Success(value=1)
```

```kotlin
AE_TRY.fromEither(Either.Left(RuntimeException("Boom")))
// Failure(exception=java.lang.RuntimeException: Boom)
```

#### catch

Constructor function. It takes two function parameters. The first is a generator function from `() -> A`. The second is an error mapping function from `(Throwable) -> E`.
`catch()` runs the generator function to generate a success datatype, and if it throws an exception it uses the error mapping function to create a new failure datatype.

```kotlin
AE_EITHER.catch({ 1 } ,::identity)
// Right(b=1)
```

```kotlin
AE_EITHER.catch({ throw RuntimeException("Boom") } ,::identity)
// Left(a=java.lang.RuntimeException: Boom)
```

### Laws

Arrow provides `ApplicativeErrorLaws` in the form of test cases for internal verification of lawful instances and third party apps creating their own `ApplicativeError` instances.

### Example : Alternative validation strategies using `ApplicativeError`

In this validation example we demonstrate how we can use `ApplicativeError` instead of `Validated` to abstract away validation strategies and raising errors in the context we are computing in.

*Model*

```kotlin
import arrow.*
import arrow.core.*
import arrow.typeclasses.*
import arrow.data.*

sealed class ValidationError(val msg: String) {
  data class DoesNotContain(val value: String) : ValidationError("Did not contain $value")
  data class MaxLength(val value: Int) : ValidationError("Exceeded length of $value")
  data class NotAnEmail(val reasons: Nel<ValidationError>) : ValidationError("Not a valid email")
}

data class FormField(val label: String, val value: String)
data class Email(val value: String)
```

*Rules*

```kotlin
sealed class Rules<F>(A: ApplicativeError<F, Nel<ValidationError>>) : ApplicativeError<F, Nel<ValidationError>> by A {

  private fun FormField.contains(needle: String): Kind<F, FormField> =
    if (value.contains(needle, false)) just(this)
    else raiseError(ValidationError.DoesNotContain(needle).nel())

  private fun FormField.maxLength(maxLength: Int): Kind<F, FormField> =
    if (value.length <= maxLength) just(this)
    else raiseError(ValidationError.MaxLength(maxLength).nel())

  fun FormField.validateEmail(): Kind<F, Email> =
    map(contains("@"), maxLength(250), {
      Email(value)
    }).handleErrorWith { raiseError(ValidationError.NotAnEmail(it).nel()) }

  object ErrorAccumulationStrategy :
    Rules<ValidatedPartialOf<Nel<ValidationError>>>(Validated.applicativeError(NonEmptyList.semigroup()))
  
  object FailFastStrategy :
    Rules<EitherPartialOf<Nel<ValidationError>>>(Either.applicativeError())
  
  companion object {
    infix fun <A> failFast(f: FailFastStrategy.() -> A): A = f(FailFastStrategy)
    infix fun <A> accumulateErrors(f: ErrorAccumulationStrategy.() -> A): A = f(ErrorAccumulationStrategy)
  }

}
```

`Rules` defines abstract behaviors that can be composed and have access to the scope of `ApplicativeError` where we can invoke `just` to lift values in to the positive result and `raiseError` into the error context.

Once we have such abstract algebra defined we can simply materialize it to data types that support different error strategies:

*Error accumulation*

```kotlin
Rules accumulateErrors {
  listOf(
    FormField("Invalid Email Domain Label", "nowhere.com"),
    FormField("Too Long Email Label", "nowheretoolong${(0..251).map { "g" }}"), //this accumulates N errors
    FormField("Valid Email Label", "getlost@nowhere.com")
  ).map { it.validateEmail() }
}
```
*Fail Fast*

```kotlin
Rules failFast {
  listOf(
    FormField("Invalid Email Domain Label", "nowhere.com"),
    FormField("Too Long Email Label", "nowheretoolong${(0..251).map { "g" }}"), //this fails fast 
    FormField("Valid Email Label", "getlost@nowhere.com")
  ).map { it.validateEmail() }
}
```

### Data Types

The following datatypes in Arrow provide instances that adhere to the `ApplicativeError` typeclass.

- [Try]({{ '/docs/datatypes/try' | relative_url }})
- [Either]({{ '/docs/datatypes/either' | relative_url }})
- [Kleisli]({{ '/docs/datatypes/kleisli' | relative_url }})
- [Option]({{ '/docs/datatypes/option' | relative_url }})
- [EitherT]({{ '/docs/datatypes/eithert' | relative_url }})
- [StateT]({{ '/docs/datatypes/statet' | relative_url }})
- [IO]({{ '/docs/effects/io' | relative_url }})
- [ObservableK]({{ '/docs/integrations/rx2' | relative_url }})
- [FlowableK]({{ '/docs/integrations/rx2' | relative_url }})
- [DeferredK]({{ '/docs/integrations/kotlinxcoroutines/' | relative_url }})
