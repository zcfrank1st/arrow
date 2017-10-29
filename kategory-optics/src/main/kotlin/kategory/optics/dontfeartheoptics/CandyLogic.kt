package kategory.optics.dontfeartheoptics

import kategory.*
import kategory.optics.*
import kategory.optics.state.*

object Alien2 {

    // PROBLEM: Crush a particular candy in the matrix, increase the score and
    // return the resulting value.

    /* 2.1: Explore the area and locate the alien */

    fun crushPos(pos: Pos): (Level) -> Tuple2<Level, Long> = { lv0 ->
        val lv1 = (levelBoard() compose boardMatrix()).modify(lv0) { mx -> mx + (pos to none()) }
        val lv2 = levelCurrentScore().modify(lv1) { cs -> cs + 1 }
        lv2 toT levelCurrentScore().get(lv2)
    }

    /* 2.2: Equip new weapons and defeat the alien */

    // State[S, A](run: S => (S, A))

    // Using state clumsily
    fun crushPos2(pos: Pos): (Level) -> Tuple2<Level, Long> = { lv0 ->
        val (lv1, _) = State<Level, Unit> { lv ->
            (levelBoard() compose boardMatrix()).modify(lv) { mx -> mx + (pos to none()) } toT Unit
        }.run(lv0)

        State<Level, Long> { lv ->
            val nlv = levelCurrentScore().modify(lv) { cs -> cs + 1 }
            nlv toT levelCurrentScore().get(nlv)
        }.run(lv1)
    }

    // State programs compose!
    fun crushPos3(pos: Pos): (Level) -> Tuple2<Level, Long> = { lv0 ->
        State<Level, Unit> { lv ->
            (levelBoard() compose boardMatrix()).modify(lv) { mx -> mx + (pos to none()) } toT Unit
        }.flatMap {
            State<Level, Long> { lv ->
                val nlv = levelCurrentScore().modify(lv) { cs -> cs + 1 }
                nlv toT levelCurrentScore().get(nlv)
            }
        }.ev().run(lv0)
    }

    // Optics integrates with State
    fun crushPop4(pos: Pos): StateT<IdHK, Level, Long> =
            (levelBoard() compose boardMatrix()).mod { mx -> mx + (pos to none()) }.flatMap { _ ->
                levelCurrentScore().mod { it + 5 }
            }.ev()

}

object Alien3 {

    // PROBLEM: Given a `Game` (instead of a `Level`) get and modify the current
    // score.

    /* 3.1: Explore the area and locate the alien */

    // Can we use lenses?
    fun getScore(): StateT<IdHK, Game, Option<Long>> = gameLevel().extract().map { olv ->
        olv.map(levelCurrentScore()::get)
    }.ev()

    fun modifyScore(f: (Long) -> Long): StateT<IdHK, Game, Unit> = gameLevel().mod_ { olv ->
        olv.map(levelCurrentScore().lift(f))
    }

    /* 3.2: Equip new weapons and defeat the alien */

    // The most typical example of a `Prism` is `some`. It focuses on the value
    // hidden by an `Option`. We'll implement it manually.

    fun <A> mySome(): Prism<Option<A>, A> = Prism(
            getOrModify = { it.fold({ it.left() }, { it.right() }) },
            reverseGet = { it.some() }
    )

    fun getScore2(): StateT<IdHK, Game, Option<Long>> =
            (gameLevel() compose somePrism() compose levelCurrentScore()).extract()

    fun modifyScore2(f: (Long) -> Long): StateT<IdHK, Game, Unit> =
            (gameLevel() compose somePrism() compose levelCurrentScore()).mod_(f)

}

object Alien4 {

    // PROBLEM: Crush a particular board column. You can use `op` in your
    // implementation.

    /* 4.1: Explore the area and locate the alien */

    val op: Optional<Game, CandyMatrix> =
            gameLevel() compose somePrism() compose levelBoard() compose boardMatrix()

    fun crushColumn(j: Int): StateT<IdHK, Game, Unit> = op.mod_ { mx ->
        mx.mapValues { (p, oc) ->
            if (p.j == j) none() else oc
        }.k()
    }

    // Using `filter` clumsily
    fun crushColumn2(j: Int): StateT<IdHK, Game, Unit> =
            op.mod_ { mx -> filterIndex<CandyMatrix, Pos, Option<Candy>>().filter { p: Pos -> p.j == j }.set(mx, none()) }

}