package kategory.optics

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll
import kategory.ListKW
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
import kategory.run
import kategory.runA

@RunWith(KTestJUnitRunner::class)
class TraversalTest : UnitSpec() {

    private val pTraversal = fromTraversable<ListKWHK, Int, Int>()

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
                pTraversal.getAll(ints)
                        .eqv(ints)
            })
        }

        "Folding all the values of a traversal" {
            forAll(genListKW(Gen.int()), { ints: ListKW<Int> ->
                pTraversal.fold(ints)
                        .eqv(ints.sum())
            })
        }

        "Combining all the values of a traversal" {
            forAll(genListKW(Gen.int()), { ints ->
                pTraversal.combineAll(ints)
                        .eqv(ints.sum())
            })
        }

        "Finding an number larger than 10" {
            forAll(Gen.list(Gen.choose(-100, 100)), { ints ->
                pTraversal.find(ints.k()) { it > 10 }
                        .eqv(ints.firstOrNull { it > 10 }?.some() ?: none())
            })
        }

        "Get the length from a traversal" {
            forAll(genListKW(Gen.int()), { ints ->
                pTraversal.size(ints)
                        .eqv(ints.size)
            })
        }

        "Lens as state should be same as getting all foci from traversal" {
            forAll(genListKW(Gen.int())) { ints ->
                pTraversal.toState().run(ints) ==
                        ints toT pTraversal.getAll(ints)
            }
        }

        "extract state from traversal should be same as getting all foci from traversal" {
            forAll(genListKW(Gen.int())) { ints ->
                pTraversal.extract().run(ints) ==
                        ints toT pTraversal.getAll(ints)
            }
        }

        "inspecting state by f from traversal should be same as mapping all values from traversal over f" {
            forAll(genListKW(Gen.int()), genFunctionAToB<ListKW<Int>, String>(Gen.string())) { ints, f ->
                pTraversal.inspect(f).run(ints) ==
                        ints toT f(pTraversal.getAll(ints))
            }
        }

        "mod state through a traversal should modify the source and return its new value" {
            forAll(genListKW(Gen.int()), genFunctionAToB<Int, Int>(Gen.int())) { ints, f ->
                val modifiedInts = pTraversal.modify(ints, f)
                //TODO eq lookup results in stack overflow
                pTraversal.mod(f).run(ints) == modifiedInts toT modifiedInts
            }
        }

        "modo state through a traversal should modify the source and return its old value" {
            forAll(genListKW(Gen.int()), genFunctionAToB<Int, Int>(Gen.int())) { ints, f ->
                val oldValue = pTraversal.getAll(ints)
                pTraversal.modo(f).run(ints) ==
                        pTraversal.modify(ints, f) toT oldValue
            }
        }

        "mod_ state through a traversal should modify the source" {
            forAll(genListKW(Gen.int()), genFunctionAToB<Int, Int>(Gen.int())) { ints, f ->
                pTraversal.mod_(f).run(ints) ==
                        pTraversal.modify(ints, f) toT Unit
            }
        }

        "assign should set the value to the state through a traversal and return it" {
            forAll(genListKW(Gen.int()), Gen.int()) { ints, i ->
                pTraversal.assign(i).run(ints) == pTraversal.set(ints, i) toT ints.map { i }
            }
        }

        "assigno should set the value to the state through a traversal and return the old value" {
            forAll(genListKW(Gen.int()), Gen.int()) { ints, i ->
                val oldValue = pTraversal.getAll(ints)
                pTraversal.assigno(i).run(ints) ==
                        pTraversal.set(ints, i) toT oldValue
            }
        }

        "assign_ should set the value to the state through a traversal and ignore the value" {
            forAll(genListKW(Gen.int()), Gen.int()) { ints, i ->
                pTraversal.assign_(i).run(ints) ==
                        pTraversal.set(ints, i) toT Unit
            }
        }

    }

}
