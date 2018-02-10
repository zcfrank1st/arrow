package arrow.effects

import arrow.core.Id
import arrow.core.monad
import arrow.core.value
import arrow.test.UnitSpec
import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.shouldBe
import kotlinx.coroutines.experimental.newSingleThreadContext
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class ComprehensionsTest : UnitSpec() {
    init {

        data class MyError(val msg: String): Exception(msg)

        "coros" {
            val pre = tn()
            Id.monad().binding(newSingleThreadContext("Thread")) {
                //pure(2).bind()
                //binding(newSingleThreadContext("1")) { tn() }.bind()
                tn()
            }.value() shouldBe "Thread"
            /*Try.monadError().bindingCatch(newSingleThreadContext("Thread")) {
                binding(newSingleThreadContext("PACO")) { throw MyError(tn()) }.bind()
            } shouldBe Try.raise<Int>(MyError("PACO"))*/
            //IO.monad().binding(newSingleThreadContext("PACO")) {
                //println("HELLO!")
                //throw MyError("111")
                //pure(tn()).bind()
                //println("HELLO2!")
                //binding(newSingleThreadContext("PECO")) { tn() }.bind()
                //tn()
            //}.ev().unsafeRunSync() shouldBe "PACO"
            //IO.effect().bindingEffect(newSingleThreadContext("PACO1")) { tn() }.a.ev().unsafeRunSync() shouldBe "PACO1"
            tn() shouldBe pre
        }
    }

    fun tn() = Thread.currentThread().name
}
