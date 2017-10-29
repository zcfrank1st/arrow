package kategory.optics.dontfeartheoptics

import kategory.*
import kategory.optics.*
import kategory.optics.state.*

fun levelOp(): Optional<Game, Level> =
        gameLevel() compose somePrism()

fun boardOp(): Optional<Game, Board> =
        levelOp() compose levelBoard()

fun targetScoreOp(): Optional<Game, Long> =
        levelOp() compose levelTargetScore()

fun currentScoreOp(): Optional<Game, Long> =
        levelOp() compose levelCurrentScore()

fun targetMovesOp(): Optional<Game, Long> =
        levelOp() compose levelTargetMoves()

fun currentMovesOp(): Optional<Game, Int> =
        levelOp() compose levelCurrentMoves()

fun heightOp(): Optional<Game, Int> =
        boardOp() compose boardHeight()

fun widthOp(): Optional<Game, Int> =
        boardOp() compose boardWidth()

fun rngOp(): Optional<Game, RNG> =
        boardOp() compose boardRng()

fun matrixOp(): Optional<Game, CandyMatrix> =
        boardOp() compose boardMatrix()

fun matrixITr(): Traversal<Game, Tuple2<Pos, Option<Candy>>> =
        matrixOp() compose mapkwToList() compose listToListKW() compose Traversal.fromTraversable() compose pairToTuple()

fun candyOp(pos: Pos): Optional<Game, Option<Option<Candy>>> = TODO("Needs At typeclass")

fun lineITr(i: Int): Traversal<Game, Tuple2<Pos, Option<Candy>>> = object : Traversal<Game, Tuple2<Pos, Option<Candy>>> {
    override fun <F> modifyF(FA: Applicative<F>, s: Game, f: (Tuple2<Pos, Option<Candy>>) -> HK<F, Tuple2<Pos, Option<Candy>>>): HK<F, Game> {
        return matrixITr().modifyF(FA, s) { (pos, candy) ->
            if (pos.i == i) f(pos toT candy) else FA.pure(pos toT candy)
        }
    }
}

fun columnITr(j: Int): Traversal<Game, Tuple2<Pos, Option<Candy>>> = object : Traversal<Game, Tuple2<Pos, Option<Candy>>> {
    override fun <F> modifyF(FA: Applicative<F>, s: Game, f: (Tuple2<Pos, Option<Candy>>) -> HK<F, Tuple2<Pos, Option<Candy>>>): HK<F, Game> {
        return matrixITr().modifyF(FA, s) { (pos, candy) ->
            if (pos.j == j) f(pos toT candy) else FA.pure(pos toT candy)
        }
    }
}

fun posRangeITr(vararg pos: Pos): Traversal<Game, Tuple2<Pos, Option<Candy>>> = object : Traversal<Game, Tuple2<Pos, Option<Candy>>> {
    override fun <F> modifyF(FA: Applicative<F>, s: Game, f: (Tuple2<Pos, Option<Candy>>) -> HK<F, Tuple2<Pos, Option<Candy>>>): HK<F, Game> {
        return matrixITr().modifyF(FA, s) { (poss, candy) ->
            if (pos.contains(poss)) f(poss toT candy) else FA.pure(poss toT candy)
        }
    }
}

fun modifyUps(f: (Int) -> Int) = State<Game, Unit> { gameUps().modify(it, f) toT Unit }

fun isIdle() = State<Game, Boolean> { it toT levelOp().isEmpty(it) }

fun isPlaying() = isIdle().map { it.not() }

fun nonZeroUps() = gameUps().extracts { it > 0 }

fun crushLine(i: Int) = crushWith(lineITr(i))

fun crushColumn(j: Int) = crushWith(columnITr(j))

fun score(crushed: Int): StateT<IdHK, Game, Unit> =
        currentScoreOp().mod_ { it + (crushed * 5) }

fun crushWith(tr: Traversal<Game, Tuple2<Pos, Option<Candy>>>): StateT<IdHK, Game, Int> = StateT.monad<IdHK, Game>().binding {
    val ps: ListKW<Tuple2<Dir, Int>> = State<Game, ListKW<Tuple2<Dir, Int>>> { game ->
        game toT tr.foldMap(game, { (pos, candy) ->
            candy.fold(
                    { emptyList<Tuple2<Dir, Int>>().k() },
                    {
                        when (it) {
                            is HorStriped -> listOf(Left toT pos.i).k()
                            is VerStriped -> listOf(Up toT pos.j).k()
                            else -> emptyList<Tuple2<Dir, Int>>().k()
                        }

                    })
        })
    }.bind()

    tr.mod { (pos, _) -> pos toT none() }.bind()

    ps.traverse { (dir, i) ->
        when (dir) {
            is Up -> crushColumn(i)
            is Left -> crushLine(i)
            else -> State { it toT 0 }
        }
    }.bind()

    val n = State { game: Game -> game toT tr.size(game) }.also {
        it.flatMap { score(it) }
    }.bind()

    yields(n)
}.ev()

