package kategory.optics

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll
import kategory.Eq
import kategory.HK
import kategory.Option
import kategory.OptionHK
import kategory.OptionKind
import kategory.SetterLaws
import kategory.UnitSpec
import kategory.eqv
import kategory.genFunctionAToB
import kategory.genOption
import kategory.getOrElse
import kategory.left
import kategory.right
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class SetterTest : UnitSpec() {

    init {
        testLaws(
            SetterLaws.laws(
                setter = Setter.id(),
                aGen = Gen.int(),
                bGen = Gen.int(),
                funcGen = genFunctionAToB(Gen.int())),

            SetterLaws.laws(
                setter = tokenSetter,
                aGen = TokenGen,
                bGen = Gen.string(),
                funcGen = genFunctionAToB(Gen.string())),

            SetterLaws.laws(
                setter = Setter.fromFunctor(),
                aGen = genOption(TokenGen),
                bGen = TokenGen,
                funcGen = genFunctionAToB(TokenGen))
        )

        "Joining two lenses together with same target should yield same result" {
            val userTokenStringSetter = userSetter compose tokenSetter
            val joinedSetter = tokenSetter.choice(userTokenStringSetter)
            val oldValue = "oldValue"
            val token = Token(oldValue)
            val user = User(token)

            forAll { value: String ->
                joinedSetter.set(token.left(), value).swap().getOrElse { Token("Wrong value") }.value
                        .eqv(joinedSetter.set(user.right(), value).getOrElse { User(Token("Wrong value")) }.token.value)
            }
        }

        "Lifting a function should yield the same result as direct modify" {
            forAll(TokenGen, Gen.string()) { token, value ->
                tokenSetter.modify(token) { value }
                        .eqv(tokenSetter.lift { value }(token))
            }
        }

    }

}
