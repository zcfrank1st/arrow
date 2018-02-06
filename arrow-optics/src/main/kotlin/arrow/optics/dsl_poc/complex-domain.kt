package arrow.optics.dsl_poc

import arrow.data.MapKW
import arrow.data.k
import arrow.lenses

sealed class Keys
object One: Keys()
object Two: Keys()
object Three: Keys()
object Four: Keys()

@lenses
data class Db(val content: MapKW<Keys, String>)

val <T> BoundSetter<T, Db>.content
    inline get() = setter(dbContent().asSetter())

val db = Db(mapOf(
        One to "one",
        Two to "two",
        Three to "three",
        Four to "four"
).k())