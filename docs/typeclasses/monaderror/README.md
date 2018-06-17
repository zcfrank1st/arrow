---
layout: docs
title: MonadError
permalink: /docs/typeclasses/monaderror/
---

## MonadError

MonadError is the typeclass used to explicitly represent errors during sequential execution.
It is parametrized to an error type `E`, which means the datatype has at least a "success" and a "failure" version.
These errors can come in the form of `Throwable`, `Exception`, or any other type hierarchy of the user's choice.

`MonadError` extends from [`ApplicativeError`]({{ '/docs/typeclasses/applicativeerror' | relative_url }}), which is already used to represent errors in independent computations. This way all the methods [`ApplicativeError`]({{ '/docs/typeclasses/applicativeerror' | relative_url }}) provides to handle recovery from errors are also available in `MonadError`.

### Main Combinators

`MonadError` inherits all the combinators available in [`ApplicativeError`]({{ '/docs/typeclasses/applicativeerror' | relative_url }}) and [`Monad`]({{ '/docs/typeclasses/monad' | relative_url }}). It also adds one of its own.

#### raiseError

Inherited from [`ApplicativeError`]({{ '/docs/typeclasses/applicativeerror' | relative_url }}). A constructor function.
It lifts an exception into the computational context of a type constructor.

```kotlin
import arrow.*
import arrow.core.*
import arrow.instances.*

ForEither<Throwable>() extensions { 
  raiseError<Int>(RuntimeException("Paco"))
}
// Left(a=java.lang.RuntimeException: Paco)
```

```kotlin
import arrow.data.*

ForTry extensions { 
  raiseError<Int>(RuntimeException("Paco"))
}
// Failure(exception=java.lang.RuntimeException: Paco)
```

```kotlin
import arrow.effects.*

ForIO extensions { 
  raiseError<Int>(RuntimeException("Paco"))
}
// RaiseError(exception=java.lang.RuntimeException: Paco)
```

#### Kind<F, A>.ensure

Tests a predicate against the object, and if it fails it executes a function to create an error.

```kotlin
ForEither<Throwable>() extensions {
  Either.Right(1).ensure({ RuntimeException("Failed predicate") }, { it > 0 }) 
}
// Right(b=1)
```

```kotlin
ForEither<Throwable>() extensions {
  Either.Right(1).ensure({ RuntimeException("Failed predicate") }, { it < 0 }) 
}
// Left(a=java.lang.RuntimeException: Failed predicate)
```

### Comprehensions

#### bindingCatch

It starts a [Monad Comprehension]({{ '/docs/patterns/monad_comprehensions' | relative_url }}) that wraps any exception thrown in the block inside `raiseError()`.

### Laws

Arrow provides `MonadErrorLaws` in the form of test cases for internal verification of lawful instances and third party apps creating their own `MonadError` instances.

### Data types

The following datatypes in Arrow provide instances that adhere to the `MonadError` typeclass.

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
