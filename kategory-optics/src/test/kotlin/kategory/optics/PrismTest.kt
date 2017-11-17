package kategory.optics

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll
import kategory.Eq
import kategory.NonEmptyList
import kategory.Option
import kategory.PrismLaws
import kategory.Try
import kategory.UnitSpec
import kategory.applicative
import kategory.compose
import kategory.genEither
import kategory.genFunctionAToB
import kategory.genTuple
import kategory.run
import kategory.runA
import kategory.some
import kategory.toT
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class PrismTest : UnitSpec() {

    init {
        testLaws(
            PrismLaws.laws(
                prism = sumPrism,
                aGen = SumGen,
                bGen = Gen.string(),
                funcGen = genFunctionAToB(Gen.string()),
                EQA = Eq.any(),
                EQB = Eq.any(),
                EQOptionB = Eq.any()),

            PrismLaws.laws(
                prism = sumPrism.first(),
                aGen = genTuple(SumGen, Gen.int()),
                bGen = genTuple(Gen.string(), Gen.int()),
                funcGen = genFunctionAToB(genTuple(Gen.string(), Gen.int())),
                EQA = Eq.any(),
                EQB = Eq.any(),
                EQOptionB = Eq.any()),

            PrismLaws.laws(
                prism = sumPrism.second(),
                aGen = genTuple(Gen.int(), SumGen),
                bGen = genTuple(Gen.int(), Gen.string()),
                funcGen = genFunctionAToB(genTuple(Gen.int(), Gen.string())),
                EQA = Eq.any(),
                EQB = Eq.any(),
                EQOptionB = Eq.any()),

            PrismLaws.laws(
                prism = sumPrism.right<SumType, SumType, String, String, Int>(),
                aGen = genEither(Gen.int(), SumGen),
                bGen = genEither(Gen.int(), Gen.string()),
                funcGen = genFunctionAToB(genEither(Gen.int(), Gen.string())),
                EQA = Eq.any(),
                EQB = Eq.any(),
                EQOptionB = Eq.any()),

            PrismLaws.laws(
                prism = sumPrism.left<SumType, SumType, String, String, Int>(),
                aGen = genEither(SumGen, Gen.int()),
                bGen = genEither(Gen.string(), Gen.int()),
                funcGen = genFunctionAToB(genEither(Gen.string(), Gen.int())),
                EQA = Eq.any(),
                EQB = Eq.any(),
                EQOptionB = Eq.any()),

            PrismLaws.laws(
                prism = Prism.id(),
                aGen = genEither(Gen.int(), Gen.int()),
                bGen = genEither(Gen.int(), Gen.int()),
                funcGen = genFunctionAToB(genEither(Gen.int(), Gen.int())),
                EQA = Eq.any(),
                EQB = Eq.any(),
                EQOptionB = Eq.any())
        )

        "Joining two prisms together with same target should yield same result" {
            forAll(SumGen, { a ->
                (sumPrism compose stringPrism).getOption(a) == sumPrism.getOption(a).flatMap(stringPrism::getOption) &&
                        (sumPrism + stringPrism).getOption(a) == (sumPrism compose stringPrism).getOption(a)
            })
        }

        "Checking if a prism exists with a target" {
            forAll(SumGen, SumGen, Gen.bool(), { a, other, bool ->
                Prism.only(a, object : Eq<SumType> {
                    override fun eqv(a: SumType, b: SumType): Boolean = bool
                }).isEmpty(other) == bool
            })
        }

        "Checking if there is no target" {
            forAll(SumGen, { sum ->
                sumPrism.isEmpty(sum) == sum !is SumType.A
            })
        }

        "Checking if a target exists" {
            forAll(SumGen, { sum ->
                sumPrism.nonEmpty(sum) == sum is SumType.A
            })
        }

        "Setting a target on a prism should set the correct target"{
            forAll(AGen, Gen.string(), { a, string ->
                sumPrism.setOption(a, string) == a.copy(string = string).some()
            })
        }

        "Finding a target using a predicate within a Lens should be wrapped in the correct option result" {
            forAll(SumGen, Gen.bool(), { sum, predicate ->
                sumPrism.find(sum) { predicate }.fold({ false }, { true }) == (predicate && sum is SumType.A)
            })
        }

        "Checking existence predicate over the target should result in same result as predicate" {
            forAll(SumGen, Gen.bool(), { sum, predicate ->
                sumPrism.exist(sum) { predicate } == (predicate && sum is SumType.A)
            })
        }

        "Checking satisfaction of predicate over the target should result in opposite result as predicate" {
            forAll(SumGen, Gen.bool(), { sum, predicate ->
                sumPrism.all(sum) { predicate } == (predicate || sum is SumType.B)
            })
        }

        "Prism as state should be same as getting value from optional" {
            forAll(SumGen) { sum ->
                sumPrism.toState().runA(sum) == sumPrism.getOption(sum)
            }
        }

        "extract state from prism should be same as getting value from prism" {
            forAll(SumGen) { sum ->
                sumPrism.extract().runA(sum) == sumPrism.getOption(sum)
            }
        }

        "inspecting by f state from prism should be same as modifying and getting value from prism over f" {
            forAll(SumGen, genFunctionAToB<String, String>(Gen.string())) { sum, f ->
                sumPrism.inspect(f).runA(sum) == (sumPrism::getOption compose sumPrism.lift(f))(sum)
            }
        }

//        "mod state through a optional should modify the source and return its new value" {
//            forAll(Gen.list(Gen.int()), genFunctionAToB<Int, Int>(Gen.int())) { ints, f ->
//                val modifiedList = optionalHead.modify(ints, f)
//                optionalHead.mod(f).run(ints) == modifiedList toT optionalHead.getOption(modifiedList)
//            }
//        }
//
//        "modo state through a optional should modify the source and return its old value" {
//            forAll(Gen.list(Gen.int()), genFunctionAToB<Int, Int>(Gen.int())) { ints, f ->
//                val oldOptionalHead = optionalHead.getOption(ints)
//                optionalHead.modo(f).run(ints) == optionalHead.modify(ints, f) toT oldOptionalHead
//            }
//        }
//
//        "mod_ state through a optional should modify the source and return ignore value" {
//            forAll(Gen.list(Gen.int()), genFunctionAToB<Int, Int>(Gen.int())) { ints, f ->
//                optionalHead.mod_(f).run(ints) == optionalHead.modify(ints, f) toT Unit
//            }
//        }
//
//        "assign should set the value to the state through a optional and return it" {
//            forAll(Gen.list(Gen.int()), Gen.int()) { ints, int ->
//                optionalHead.assign(int).run(ints) == optionalHead.set(ints, int) toT int
//            }
//        }
//
//        "assigno should set the value to the state through a optional and return the old value" {
//            forAll(Gen.list(Gen.int()), Gen.int()) { ints, int ->
//                val oldOptionalHead = optionalHead.getOption(ints)
//                optionalHead.assigno(int).run(ints) == optionalHead.set(ints, int) toT oldOptionalHead
//            }
//        }
//
//        "assign_ should set the value to the state through a optional and ignore the value" {
//            forAll(Gen.list(Gen.int()), Gen.int()) { ints, int ->
//                optionalHead.assign_(int).run(ints) == optionalHead.set(ints, int) toT Unit
//            }
//        }

    }

}
