package kategory.optics

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll
import org.junit.runner.RunWith
import kategory.*

@RunWith(KTestJUnitRunner::class)
class GetterTest : UnitSpec() {

    init {

        val userGetter = userIso.asGetter()
        val tokenGetter = tokenIso.asGetter()
        val length = Getter<String, Int> { it.length }
        val upper = Getter<String, String> { it.toUpperCase() }

        "Getting the target should always yield the exact result" {
            forAll({ value: String ->
                tokenGetter.get(Token(value)) == value
            })
        }

        "Finding a target using a predicate within a Getter should be wrapped in the correct option result" {
            forAll({ value: String, predicate: Boolean ->
                tokenGetter.find(Token(value)) { predicate }.fold({ false }, { true }) == predicate
            })
        }

        "Checking existence of a target should always result in the same result as predicate" {
            forAll({ value: String, predicate: Boolean ->
                tokenGetter.exist(Token(value)) { predicate } == predicate
            })
        }

        "Zipping two lenses should yield a tuple of the targets" {
            forAll({ value: String ->
                length.zip(upper).get(value) == value.length toT value.toUpperCase()
            })
        }

        "Joining two getters together with same target should yield same result" {
            val userTokenStringGetter = userGetter compose tokenGetter
            val joinedGetter = tokenGetter.choice(userTokenStringGetter)

            forAll({ tokenValue: String ->
                val token = Token(tokenValue)
                val user = User(token)
                joinedGetter.get(token.left()) == joinedGetter.get(user.right())
            })
        }

        "Pairing two disjoint getters should yield a pair of their results" {
            val splitGetter: Getter<Tuple2<Token, User>, Tuple2<String, Token>> = tokenGetter.split(userGetter)
            forAll(TokenGen, UserGen, { token: Token, user: User ->
                splitGetter.get(token toT user) == token.value toT user.token
            })
        }

        "Creating a first pair with a type should result in the target to value" {
            val first = tokenGetter.first<Int>()
            forAll(TokenGen, Gen.int(), { token: Token, int: Int ->
                first.get(token toT int) == token.value toT int
            })
        }

        "Creating a second pair with a type should result in the value target" {
            val first = tokenGetter.second<Int>()
            forAll(Gen.int(), TokenGen, { int: Int, token: Token ->
                first.get(int toT token) == int toT token.value
            })
        }

        "Getter as reader should be same as getting value from getter" {
            forAll(TokenGen) { token ->
                tokenGetter.toReader().runId(token)
                        .eqv(tokenGetter.get(token))
            }
        }

        "ask value from getter should be same as getting value from getter" {
            forAll(TokenGen) { token ->
                tokenGetter.ask().runId(token)
                        .eqv(tokenGetter.get(token))
            }
        }

        "asks value by f from getter should be same as getting value from getter" {
            forAll(TokenGen, genFunctionAToB<String, String>(Gen.string())) { token, f ->
                tokenGetter.asks(f).runId(token)
                        .eqv(tokenGetter.get(token).let(f))
            }
        }

        "Getter as state should be same as getting value from getter" {
            forAll(TokenGen) { token ->
                tokenGetter.toState().run(token)
                        .eqv(token toT tokenLens.get(token))
            }
        }

        "extract state from getter should be same as getting value from getter" {
            forAll(TokenGen) { token ->
                tokenGetter.extract().run(token)
                        .eqv(token toT tokenLens.get(token))
            }
        }

        "inspecting by f state from getter should be same as modifying and getting value from getter over f" {
            forAll(TokenGen, genFunctionAToB<String, String>(Gen.string())) { token, f ->
                tokenGetter.inspect(f).run(token)
                        .eqv(token toT tokenLens.get(token).let(f))
            }
        }

    }

}