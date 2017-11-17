package kategory.optics

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll
import kategory.*
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class OptionalTest : UnitSpec() {

    init {

        testLaws(
            OptionalLaws.laws(
                optional = optionalHead,
                aGen = Gen.list(Gen.int()),
                bGen = Gen.int(),
                funcGen = genFunctionAToB(Gen.int()),
                EQA = Eq.any(),
                EQB = Eq.any(),
                EQOptionB = Eq.any()),

            OptionalLaws.laws(
                optional = Optional.id(),
                aGen = Gen.int(),
                bGen = Gen.int(),
                funcGen = genFunctionAToB(Gen.int()),
                EQA = Eq.any(),
                EQB = Eq.any(),
                EQOptionB = Eq.any()),

            OptionalLaws.laws(
                optional = optionalHead.first(),
                aGen = genTuple(Gen.list(Gen.int()), Gen.bool()),
                bGen = genTuple(Gen.int(), Gen.bool()),
                funcGen = genFunctionAToB(genTuple(Gen.int(), Gen.bool())),
                EQA = Eq.any(),
                EQB = Eq.any(),
                EQOptionB = Eq.any()),

            OptionalLaws.laws(
                optional = optionalHead.second(),
                aGen = genTuple(Gen.bool(), Gen.list(Gen.int())),
                bGen = genTuple(Gen.bool(), Gen.int()),
                funcGen = genFunctionAToB(genTuple(Gen.bool(), Gen.int())),
                EQA = Eq.any(),
                EQB = Eq.any(),
                EQOptionB = Eq.any())
        )

        "void should always " {
            forAll({ string: String ->
                Optional.void<String, Int>().getOption(string) == None
            })
        }

        "void should always return source when setting target" {
            forAll({ int: Int, string: String ->
                Optional.void<String, Int>().set(string, int) == string
            })
        }

        "Checking if there is no target" {
            forAll(Gen.list(Gen.int()), { list ->
                optionalHead.nonEmpty(list) == list.isNotEmpty()
            })
        }

        "Lift should be consistent with modify" {
            forAll(Gen.list(Gen.int()), { list ->
                val f = { i: Int -> i + 5 }
                optionalHead.lift(f)(list) == optionalHead.modify(list, f)
            })
        }

        "LiftF should be consistent with modifyF" {
            forAll(Gen.list(Gen.int()), genTry(Gen.int()), { list, tryInt ->
                val f = { _: Int -> tryInt }
                optionalHead.liftF(f, Try.applicative())(list) == optionalHead.modifyF(list, f, Try.applicative())
            })
        }

        "Checking if a target exists" {
            forAll(Gen.list(Gen.int()), { list ->
                optionalHead.isEmpty(list) == list.isEmpty()
            })
        }

        "Finding a target using a predicate should be wrapped in the correct option result" {
            forAll(Gen.list(Gen.int()), Gen.bool(), { list, predicate ->
                optionalHead.find(list) { predicate }.fold({ false }, { true }) == predicate
            })
        }

        "Checking existence predicate over the target should result in same result as predicate" {
            forAll(Gen.list(Gen.int()), Gen.bool(), { list, predicate ->
                optionalHead.exists(list) { predicate } == predicate
            })
        }

        "Checking satisfaction of predicate over the target should result in opposite result as predicate" {
            forAll(Gen.list(Gen.int()), Gen.bool(), { list, predicate ->
                optionalHead.all(list) { predicate } == predicate
            })
        }

        "Joining two optionals together with same target should yield same result" {
            val joinedOptional = optionalHead.choice(defaultHead)

            forAll(Gen.int(), { int ->
                joinedOptional.getOption(listOf(int).left()) == joinedOptional.getOption(int.right())
            })
        }

        "Optional as state should be same as getting value from optional" {
            forAll(Gen.list(Gen.int())) { ints ->
                optionalHead.toState().runA(ints) == optionalHead.getOption(ints)
            }
        }

        "extract state from optional should be same as getting value from optional" {
            forAll(Gen.list(Gen.int())) { ints ->
                optionalHead.extract().runA(ints) == optionalHead.getOption(ints)
            }
        }

        "inspecting by f state from optional should be same as modifying and getting value from optional over f" {
            forAll(Gen.list(Gen.int()), genFunctionAToB<Int, Int>(Gen.int())) { ints, f ->
                optionalHead.inspect(f).runA(ints) == (optionalHead::getOption compose optionalHead.lift(f))(ints)
            }
        }

        "mod state through a optional should modify the source and return its new value" {
            forAll(Gen.list(Gen.int()), genFunctionAToB<Int, Int>(Gen.int())) { ints, f ->
                val modifiedList = optionalHead.modify(ints, f)
                optionalHead.mod(f).run(ints) == modifiedList toT optionalHead.getOption(modifiedList)
            }
        }

        "modo state through a optional should modify the source and return its old value" {
            forAll(Gen.list(Gen.int()), genFunctionAToB<Int, Int>(Gen.int())) { ints, f ->
                val oldOptionalHead = optionalHead.getOption(ints)
                optionalHead.modo(f).run(ints) == optionalHead.modify(ints, f) toT oldOptionalHead
            }
        }

        "mod_ state through a optional should modify the source and return ignore value" {
            forAll(Gen.list(Gen.int()), genFunctionAToB<Int, Int>(Gen.int())) { ints, f ->
                optionalHead.mod_(f).run(ints) == optionalHead.modify(ints, f) toT Unit
            }
        }

        "assign should set the value to the state through a optional and return it" {
            forAll(Gen.list(Gen.int()), Gen.int()) { ints, int ->
                val assigned = if (optionalHead.nonEmpty(ints)) int.some() else none()
                optionalHead.assign(int).run(ints) == optionalHead.set(ints, int) toT assigned
            }
        }

        "assigno should set the value to the state through a optional and return the old value" {
            forAll(Gen.list(Gen.int()), Gen.int()) { ints, int ->
                val oldOptionalHead = optionalHead.getOption(ints)
                optionalHead.assigno(int).run(ints) == optionalHead.set(ints, int) toT oldOptionalHead
            }
        }

        "assign_ should set the value to the state through a optional and ignore the value" {
            forAll(Gen.list(Gen.int()), Gen.int()) { ints, int ->
                optionalHead.assign_(int).run(ints) == optionalHead.set(ints, int) toT Unit
            }
        }

    }

}
