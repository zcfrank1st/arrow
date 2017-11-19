package kategory.optics

import io.kotlintest.properties.Gen
import kategory.Eq
import kategory.ListKW
import kategory.autofold
import kategory.deriving
import kategory.identity
import kategory.instance
import kategory.k
import kategory.left
import kategory.right

@autofold sealed class SumType {
    companion object {}

    data class A(val string: String) : SumType() {
        companion object
    }

    data class B(val int: Int) : SumType() {
        companion object
    }
}

@instance(SumType::class)
interface SumTypeEqInstance : Eq<SumType> {

    override fun eqv(a: SumType, b: SumType): Boolean = a.fold({ aa ->
        b.fold({ ba ->
            aa.string == ba.string
        }, { _ -> false })
    }, { ab ->
        b.fold({ _ -> false }, { bb ->
            ab.int == bb.int
        })
    })

}

@instance(SumType.A::class)
interface AEqInstance : Eq<SumType.A> {
    override fun eqv(a: SumType.A, b: SumType.A): Boolean = a == b

}

@instance(SumType.B::class)
interface BEqInstance : Eq<SumType.B> {
    override fun eqv(a: SumType.B, b: SumType.B): Boolean = a == b

}

object AGen : Gen<SumType.A> {
    override fun generate(): SumType.A = SumType.A(Gen.string().generate())
}

object SumGen : Gen<SumType> {
    override fun generate(): SumType = Gen.oneOf(AGen, Gen.create { SumType.B(Gen.int().generate()) }).generate()
}

val sumPrism: Prism<SumType, String> = Prism(
        {
            when (it) {
                is SumType.A -> it.string.right()
                else -> it.left()
            }
        },
        SumType::A
)

val stringPrism: Prism<String, List<Char>> = Prism(
        { it.toList().right() },
        { it.joinToString(separator = "") }
)

internal val tokenLens: Lens<Token, String> = Lens(
        { token: Token -> token.value },
        { value: String -> { token: Token -> token.copy(value = value) } }
)

internal val tokenIso: Iso<Token, String> = Iso(
        { token: Token -> token.value },
        ::Token
)

internal val tokenSetter: Setter<Token, String> = Setter { s ->
    { token -> token.copy(value = s(token.value)) }
}

internal val userIso: Iso<User, Token> = Iso(
        { user: User -> user.token },
        ::User
)

internal val userSetter: Setter<User, Token> = Setter { s ->
    { user -> user.copy(token = s(user.token)) }
}


data class Token(val value: String) {
    companion object
}

@instance(Token::class)
interface TokenEqInstance : Eq<Token> {

//    fun EQS(): Eq<String>

    override fun eqv(a: Token, b: Token): Boolean = a == b
//            EQS().eqv(a.value, b.value)
}

internal object TokenGen : Gen<Token> {
    override fun generate() = Token(Gen.string().generate())
}

data class User(val token: Token) {
    companion object
}

@instance(User::class)
interface UserEqInstance : Eq<User> {

//    fun EQT(): Eq<Token>

    override fun eqv(a: User, b: User): Boolean = a == b
//            EQT().eqv(a.token, b.token)
}

internal object UserGen : Gen<User> {
    override fun generate() = User(TokenGen.generate())
}

internal val userLens: Lens<User, Token> = Lens(
        { user: User -> user.token },
        { token: Token -> { user: User -> user.copy(token = token) } }
)

internal val optionalHead: Optional<List<Int>, Int> = Optional(
        { it.firstOrNull()?.right() ?: it.left() },
        { int -> { list -> listOf(int) + if (list.size > 1) list.drop(1) else emptyList() } }
)

internal val defaultHead: Optional<Int, Int> = Optional(
        { it.right() },
        { ::identity }
)