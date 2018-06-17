---
layout: docs
title: At
permalink: /docs/optics/at/
---

## At

`At` provides a [Lens]({{ '/docs/optics/lens' | relative_url }}) for a structure `S` to focus in `A` at a given index `I`.

### Example

If for a structure `S` the focus `A` can be indexed by `I` then `At` can create an `Lens` with focus at `S` for a given index `I`.
We can use that `Lens` to operate on that focus `S` to get, set and modify the focus at a given index `I`.

A `MapK<Int, String>` can be indexed by its keys `Int` but not for every index an entry can be found.

```kotlin
import arrow.core.*
import arrow.data.*
import arrow.optics.typeclasses.*

val mapAt = At.at(MapK.at<Int, String>(), 2)

val map = mapOf(
            1 to "one",
            2 to "two",
            3 to "three"
    ).k()

mapAt.set(map, "new value".some())
// MapK(map={1=one, 2=new value, 3=three})
```

By setting an empty value for a key we delete that entry by removing the value.

```kotlin
mapAt.set(map, none())
// MapK(map={1=one, 3=three})
```

#### Creating your own `At` instances

Arrow provides `At` instances for some common datatypes in Arrow that can be indexed. You can look them up by calling `At.at()`.

You may create instances of `At` for your own datatypes which you will be able to use as demonstrated in the [example](#example) above.

See [Deriving and creating custom typeclass]({{ '/docs/patterns/glossary' | relative_url }}) to provide your own `At` instances for custom datatypes.

### Instances

The following datatypes in Arrow provide instances that adhere to the `At` typeclass.

- [SetK]({{ '/docs/datatypes/setk' | relative_url }})
- [MapK]({{ '/docs/datatypes/mapk' | relative_url }})
