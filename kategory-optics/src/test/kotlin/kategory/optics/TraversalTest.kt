package kategory.optics

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll
import kategory.Eq
import kategory.ListKWHK
import kategory.TraversalLaws
import kategory.UnitSpec
import kategory.eqv
import kategory.genFunctionAToB
import kategory.genListKW
import kategory.genTuple
import kategory.k
import kategory.none
import kategory.some
import kategory.toT
import org.junit.runner.RunWith
import kategory.optics.PTraversal.Companion.fromTraversable

@RunWith(KTestJUnitRunner::class)
class TraversalTest : UnitSpec() {

    init {

        testLaws(
            TraversalLaws.laws(
                traversal = fromTraversable(),
                aGen = Gen.create { Gen.list(Gen.int()).generate().k() },
                bGen = Gen.int(),
                funcGen = genFunctionAToB(Gen.int())),

            TraversalLaws.laws(
                traversal = Traversal({ it.a }, { it.b }, { a, b, _ -> a toT b }),
                aGen = genTuple(Gen.float(), Gen.float()),
                bGen = Gen.float(),
                funcGen = genFunctionAToB(Gen.float()))
        )

        "Getting all targets of a traversal" {
            forAll(genListKW(Gen.int()), { ints ->
                fromTraversable<ListKWHK, Int, Int>().getAll(ints.k())
                        .eqv(ints.k())
            })
        }

        "Folding all the values of a traversal" {
            forAll(genListKW(Gen.int()), { ints ->
                fromTraversable<ListKWHK, Int, Int>().fold(ints.k())
                        .eqv(ints.sum())
            })
        }

        "Combining all the values of a traversal" {
            forAll(genListKW(Gen.int()), { ints ->
                fromTraversable<ListKWHK, Int, Int>().combineAll(ints.k())
                        .eqv(ints.sum())
            })
        }

        "Finding an number larger than 10" {
            forAll(genListKW(Gen.choose(-100, 100)), { ints ->
                fromTraversable<ListKWHK, Int, Int>().find(ints.k()) { it > 10 }
                        .eqv(ints.firstOrNull { it > 10 }?.some() ?: none())
            })
        }

        "Get the length from a traversal" {
            forAll(genListKW(Gen.int()), { ints ->
                fromTraversable<ListKWHK, Int, Int>().size(ints.k())
                        .eqv(ints.size)
            })
        }

    }

}
