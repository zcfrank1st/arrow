package kategory.optics.instances

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.properties.Gen
import kategory.ListKW
import kategory.MapKW
import kategory.NonEmptyList
import kategory.SequenceKW
import kategory.TraversalLaws
import kategory.UnitSpec
import kategory.genChars
import kategory.genFunctionAToB
import kategory.genIntSmall
import kategory.genListKW
import kategory.genMapKW
import kategory.genNonEmptyList
import kategory.genSequenceKW
import kategory.optics.filterIndex
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class FilterWithIndexInstanceTest : UnitSpec() {

    init {

        "instances can be resolved implicitly" {
            filterIndex<ListKW<String>, Int, String>() shouldNotBe null
            filterIndex<NonEmptyList<String>, Int, String>() shouldNotBe null
            filterIndex<SequenceKW<Char>, Int, Char>() shouldNotBe null
            filterIndex<MapKW<Char, Int>, String, Int>() shouldNotBe null
            filterIndex<String, Int, Char>() shouldNotBe null
        }

        testLaws(TraversalLaws.laws(
                traversal = filterIndex<ListKW<String>, Int, String> { true },
                aGen = genListKW(Gen.string()),
                bGen = Gen.string(),
                funcGen = genFunctionAToB(Gen.string())
        ))

        testLaws(TraversalLaws.laws(
                traversal = filterIndex<NonEmptyList<String>, Int, String> { true },
                aGen = genNonEmptyList(Gen.string()),
                bGen = Gen.string(),
                funcGen = genFunctionAToB(Gen.string())
        ))

        testLaws(TraversalLaws.laws(
                traversal = filterIndex<SequenceKW<Char>, Int, Char>().filter { true },
                aGen = genSequenceKW(genChars()),
                bGen = genChars(),
                funcGen = genFunctionAToB(genChars())
        ))

        testLaws(TraversalLaws.laws(
                traversal = filterIndex<MapKW<Char, Int>, Char, Int> { true },
                aGen = genMapKW(genChars(), genIntSmall()),
                bGen = Gen.int(),
                funcGen = genFunctionAToB(Gen.int())
        ))

        testLaws(TraversalLaws.laws(
                traversal = filterIndex<String, Int, Char> { true },
                aGen = Gen.string(),
                bGen = genChars(),
                funcGen = genFunctionAToB(genChars())
        ))

    }

}