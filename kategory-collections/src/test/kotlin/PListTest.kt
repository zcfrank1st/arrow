import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.shouldNotBe
import kategory.*
import kategory.collections.*
import org.junit.Ignore
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class PListTest : UnitSpec() {
    val applicative = PList.applicative()

    init {
//
//        "instances can be resolved implicitly" {
//            functor<PListHK>() shouldNotBe null
//            applicative<PListHK>() shouldNotBe null
//            monad<PListHK>() shouldNotBe null
//            foldable<PListHK>() shouldNotBe null
//            traverse<PListHK>() shouldNotBe null
//            semigroupK<PListHK>() shouldNotBe null
//            monoidK<PListHK>() shouldNotBe null
//        }
//
//        testLaws(MonadLaws.laws(PList.monad(), Eq.any()))
//        testLaws(SemigroupKLaws.laws(PList.semigroupK(), applicative, Eq.any()))
//        testLaws(MonoidKLaws.laws(PList.monoidK(), applicative, Eq.any()))
//        testLaws(TraverseLaws.laws(PList.traverse(), applicative, { n: Int -> PList.pure(n) }, Eq.any()))

    }
}