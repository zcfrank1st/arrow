package kategory.optics.dontfeartheoptics

import kategory.*

object Color {
    val ANSI_RESET = "\u001B[0m"
    val ANSI_BLACK = "\u001B[30m"
    val ANSI_RED = "\u001B[31m"
    val ANSI_GREEN = "\u001B[32m"
    val ANSI_YELLOW = "\u001B[33m"
    val ANSI_BLUE = "\u001B[34m"
    val ANSI_PURPLE = "\u001B[35m"
    val ANSI_CYAN = "\u001B[36m"
    val ANSI_WHITE = "\u001B[37m"
}

interface RNG {

    companion object {
        fun simple(seed: Long): RNG = object : RNG {
            override fun nextInt(): Tuple2<Int, RNG> {
                val seed2 = (seed * 0x5DEECE66DL + 0xBL) and ((1L shl 48) - 1)
                return (seed2 ushr 16).toInt() toT simple(seed2)
            }
        }
    }

    fun nextInt(): Tuple2<Int, RNG>
}

fun cartesian(h: Int, w: Int): List<Tuple2<Int, Int>> = ListKW.monad().binding {
    val i = (1..h).toList().k().bind()
    val j = (1..w).toList().k().bind()
    yields(i toT j)
}.ev()

//    /* scalaz */
//
//    implicit class IfMHelper(mb: State[Game, Boolean]) {
//        def ifM_(mu: State[Game, Unit]): State[Game, Unit] =
//        mb.ifM(mu, ().point[State[Game, ?]])
//    }
//
//     XXX: not in scalaz?
//    def iterateWhile[A](a: A)(f: A => A, p: A => Boolean): List[A] =
//    if (p(a)) a :: iterateWhile(f(a))(f, p) else Nil
