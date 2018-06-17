---
layout: docs
title: Rx2
permalink: /docs/integrations/rx2/
---

## RxJava 2

Arrow aims to enhance the user experience when using RxJava. While providing other datatypes that are capable of handling effects, like IO, the style of programming encouraged by the library allows users to generify behavior for any existing abstractions.

One of such abstractions is RxJava, a library focused on providing composable streams that enable reactive programming. Observable streams are created by chaining operators into what are called observable chains.

```kotlin
Observable.from(7, 4, 11, 3)
  .map { it + 1 }
  .filter { it % 2 == 0 }
  .scan { acc, value -> acc + value }
  .toList()
  .subscribeOn(Schedulers.computation())
  .blockingFirst()
//[8, 20, 24]
```

### Integration with your existing Observable chains

The largest quality of life improvement when using Observables in Arrow is the introduction of the [Monad Comprehension]({{ '/docs/patterns/monad_comprehensions' | relative_url }}). This library construct allows expressing asynchronous Observable sequences as synchronous code using binding/bind.

#### Arrow Wrapper

To wrap any existing Observable in its Arrow Wrapper counterpart you can use the extension function `k()`.

```kotlin
import arrow.effects.*
import io.reactivex.*
import io.reactivex.subjects.*

val obs = Observable.fromArray(1, 2, 3, 4, 5).k()
obs
// ObservableK(observable=io.reactivex.internal.operators.observable.ObservableFromArray@7d8718dd)
```

```kotlin
val flow = Flowable.fromArray(1, 2, 3, 4, 5).k()
flow
// FlowableK(flowable=io.reactivex.internal.operators.flowable.FlowableFromArray@4b5da970)
```

```kotlin
val single = Single.fromCallable { 1 }.k()
single
// SingleK(single=io.reactivex.internal.operators.single.SingleFromCallable@5f3f43e7)
```

```kotlin
val maybe = Maybe.fromCallable { 1 }.k()
maybe
// MaybeK(maybe=io.reactivex.internal.operators.maybe.MaybeFromCallable@8e666e4)
```

```kotlin
val subject = PublishSubject.create<Int>().k()
subject
// ObservableK(observable=io.reactivex.subjects.PublishSubject@15216198)
```

You can return to their regular forms using the function `value()`.

```kotlin
obs.value()
// io.reactivex.internal.operators.observable.ObservableFromArray@7d8718dd
```

```kotlin
flow.value()
// io.reactivex.internal.operators.flowable.FlowableFromArray@4b5da970
```

```kotlin
single.value()
// io.reactivex.internal.operators.single.SingleFromCallable@5f3f43e7
```

```kotlin
maybe.value()
// io.reactivex.internal.operators.maybe.MaybeFromCallable@8e666e4
```

```kotlin
subject.value()
// io.reactivex.subjects.PublishSubject@15216198
```

### Observable comprehensions

The library provides instances of [`MonadError`]({{ '/docs/typeclasses/monaderror' | relative_url }}) and [`MonadDefer`]({{ '/docs/effects/monaddefer' | relative_url }}).

[`MonadDefer`]({{ '/docs/effects/async' | relative_url }}) allows you to generify over datatypes that can run asynchronous code. You can use it with `ObservableK`, `FlowableK` or `SingleK`.

```kotlin
fun <F> getSongUrlAsync(MS: MonadDefer<F>) =
  MS { getSongUrl() }

val songObservable: ObservableKOf<Url> = getSongUrlAsync(ObservableK.monadDefer())
val songFlowable: FlowableKOf<Url> = getSongUrlAsync(FlowableK.monadDefer())
val songSingle: SingleKOf<Url> = getSongUrlAsync(SingleK.monadDefer())
val songMaybe: MaybeKOf<Url> = getSongUrlAsync(MaybeK.monadDefer())
```

[`MonadError`]({{ '/docs/typeclasses/monaderror' | relative_url }}) can be used to start a [Monad Comprehension]({{ '/docs/patterns/monad_comprehensions' | relative_url }}) using the method `bindingCatch`, with all its benefits.

Let's take an example and convert it to a comprehension. We'll create an observable that loads a song from a remote location, and then reports the current play % every 100 milliseconds until the percentage reaches 100%:

```kotlin
getSongUrlAsync()
  .map { songUrl -> MediaPlayer.load(songUrl) }
  .flatMap {
    val totalTime = musicPlayer.getTotaltime()
    Observable.interval(100, Milliseconds)
      .flatMap {
        Observable.create { musicPlayer.getCurrentTime() }
          .subscribeOn(AndroidSchedulers.mainThread())
          .map { tick -> (tick / totalTime * 100).toInt() }
      }
      .takeUntil { percent -> percent >= 100 }
      .observeOn(Schedulers.immediate())
  }
```

When rewritten using `bindingCatch` it becomes:

```kotlin
import arrow.effects.*
import arrow.typeclasses.*

ForObservableK extensions { 
 bindingCatch {
  val songUrl = getSongUrlAsync().bind()
  val musicPlayer = MediaPlayer.load(songUrl)
  val totalTime = musicPlayer.getTotaltime()

  val end = PublishSubject.create<Unit>()
  Observable.interval(100, Milliseconds).takeUntil(end).bind()

  val tick = bindIn(UI) { musicPlayer.getCurrentTime() }
  val percent = (tick / totalTime * 100).toInt()
  if (percent >= 100) {
    end.onNext(Unit)
  }

  percent
 }.fix()
}
```

Note that any unexpected exception, like `AritmeticException` when `totalTime` is 0, is automatically caught and wrapped inside the observable.

### Subscription and cancellation

Observables created with comprehensions like `bindingCatch` behave the same way regular observables do, including cancellation by disposing the subscription.

```kotlin
val disposable =
  songObservable.value()
    .subscribe({ Log.d("Song $it") } , { println("Error $it") })

disposable.dispose()
```

Note that [`MonadDefer`]({{ '/docs/effects/monaddefer' | relative_url }}) provides an alternative to `bindingCatch` called `bindingCancellable` returning a `arrow.Disposable`.
Invoking this `Disposable` causes an `BindingCancellationException` in the chain which needs to be handled by the subscriber, similarly to what `Deferred` does.

```kotlin
val (observable, disposable) =
  ObservableK.monadDefer().bindingCancellable {
    val userProfile = Observable.create { getUserProfile("123") }
    val friendProfiles = userProfile.friends().map { friend ->
        bindDefer { getProfile(friend.id) }
    }
    listOf(userProfile) + friendProfiles
  }

observable.value()
  .subscribe({ Log.d("User $it") } , { println("Boom! caused by $it") })

disposable()
// Boom! caused by BindingCancellationException
```

## Available Instances

* [Applicative]({{ '/docs/typeclasses/applicative' | relative_url }})
* [ApplicativeError]({{ '/docs/typeclasses/applicativeerror' | relative_url }})
* [Functor]({{ '/docs/typeclasses/functor' | relative_url }})
* [Monad]({{ '/docs/typeclasses/monad' | relative_url }})
* [MonadError]({{ '/docs/typeclasses/monaderror' | relative_url }})
* [MonadDefer]({{ '/docs/effects/monaddefer' | relative_url }})
* [Async]({{ '/docs/effects/async' | relative_url }})
* [Effect]({{ '/docs/effects/effect' | relative_url }})
* [Foldable]({{ '/docs/typeclasses/foldable' | relative_url }})
* [Traverse]({{ '/docs/typeclasses/traverse' | relative_url }})
