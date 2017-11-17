package kategory

object UnitEqInstance : Eq<Unit> {
    override fun eqv(a: Unit, b: Unit): Boolean = true
}

object UnitEqInstanceImplicits {
    @JvmStatic fun instance(): Eq<Unit> = UnitEqInstance
}