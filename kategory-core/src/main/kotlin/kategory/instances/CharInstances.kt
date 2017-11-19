package kategory

object CharacterEqInstance : Eq<Char> {
    override fun eqv(a: Char, b: Char): Boolean = a == b
}

object CharacterEqInstanceImplicits {
    @JvmStatic
    fun instance(): CharacterEqInstance = CharacterEqInstance
}