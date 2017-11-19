package java_util

import kategory.Eq

interface ListEqInstance<A> : Eq<List<A>> {

    fun EQ(): Eq<A>

    override fun eqv(a: List<A>, b: List<A>): Boolean =
            a.zip(b) { aa, bb -> EQ().eqv(aa, bb) }.fold(true) { acc, bool ->
                acc && bool
            }

}

object ListEqInstanceImplicits {

    @JvmStatic fun <A> instance(EQ: kategory.Eq<A>): ListEqInstance<A> =
            object : ListEqInstance<A> {
                override fun EQ(): kategory.Eq<A> = EQ
            }
}
