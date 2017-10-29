package kategory.optics.dontfeartheoptics

import kategory.*
import kategory.optics.*

@lenses data class Game(
        val user: String,
        val ups: Int,
        val level: Option<Level> = Option.empty(),
        val current: Int = 0
)

@lenses data class Level(
        val targetScore: Long,
        val targetMoves: Long,
        val board: Board,
        val currentScore: Long = 0,
        val currentMoves: Int = 0
) {
    companion object {
        val MINIMAL = 2000L
        val INCREMENT = 10L
        val MOVES = 30L

        operator fun invoke(level: Int): Level =
                Level(MINIMAL + INCREMENT * level, MOVES, Board())
    }
}

@lenses data class Board(
        val height: Int,
        val width: Int,
        val rng: RNG,
        val matrix: CandyMatrix
) {
    companion object {
        val HEIGHT = 5
        val WIDTH = 8

        operator fun invoke(height: Int = HEIGHT, width: Int = WIDTH): Board = Board(
                height,
                width,
                RNG.simple(0),
                cartesian(height, width)
                        .map { Pos(it.a, it.b) }
                        .foldRight(emptyMap<Pos, Option<Candy>>().k()) { p, m ->
                            (m + (p to none())).k()
                        }
        )
    }
}

object Alien1 {

    /* 1.1: Explore the area and locate the alien */

    fun getScore(): (Level) -> Long = { lvl ->
        lvl.currentScore
    }

    fun modifyScore(f: (Long) -> Long): (Level) -> Level = { lvl ->
        lvl.copy(currentScore = f(lvl.currentScore))
    }

    fun getMatrix(): (Level) -> CandyMatrix = { lvl ->
        lvl.board.matrix
    }

    fun modifyMatrix(f: (CandyMatrix) -> CandyMatrix): (Level) -> Level = { lvl ->
        lvl.copy(
                board = lvl.board.copy(
                        matrix = f(lvl.board.matrix)
                )
        )
    }

    /* 1.2: Equip new weapons and defeat the alien */

    // Implement these lenses, that focus on particular case class fields.

    val _currentScore: Lens<Level, Long> = Lens(
            get = Level::currentScore,
            set = { cs -> { lvl -> lvl.copy(currentScore = cs) } }
    )

    val _board: Lens<Level, Board> = Lens(
            get = Level::board,
            set = { brd -> { lvl -> lvl.copy(board = brd) } }
    )

    val _matrix: Lens<Board, CandyMatrix> = Lens(
            get = Board::matrix,
            set = { mtx -> { brd -> brd.copy(matrix = mtx) } }
    )

    // Now, use auto-generated lenses to implement the previous methods.

    fun getScore2(): (Level) -> Long =
            _currentScore::get

    fun modifyScore2(f: (Long) -> Long): (Level) -> Level =
            _currentScore.lift(f)

    // Lenses compose!

    fun getMatrix2(): (Level) -> CandyMatrix =
            (_board compose _matrix)::get

    fun modifyMatrix2(f: (CandyMatrix) -> CandyMatrix): (Level) -> Level =
            (_board compose _matrix).lift(f)

}

@prisms sealed class Candy
@prisms sealed class KindedCandy : Candy()
@prisms sealed class StripedCandy : KindedCandy()
@lenses data class HorStriped(val candy: RegularCandy) : StripedCandy()
@lenses data class VerStriped(val candy: RegularCandy) : StripedCandy()
object ColourBomb : Candy()
@prisms sealed class RegularCandy : KindedCandy()
object Red : RegularCandy()
object Orange : RegularCandy()
object Yellow : RegularCandy()
object Green : RegularCandy()
object Blue : RegularCandy()
object Purple : RegularCandy()

fun Candy.kind(): Option<RegularCandy> = when (this) {
    ColourBomb -> none()
    is HorStriped -> candy.some()
    is VerStriped -> candy.some()
    is RegularCandy -> this.some()
}

fun Candy.shareKind(other: Candy): Boolean = Option.applicative()
        .map2(kind(), other.kind()) { (a, b) ->
            a == b
        }.ev().getOrElse { false }

fun Candy.hasKind(kind: RegularCandy): Boolean =
        kind().fold({ false }, { it == kind })

fun Candy.morph(f: (RegularCandy) -> StripedCandy): Candy = when (this) {
    is StripedCandy -> this
    is ColourBomb -> this
    is RegularCandy -> f(this)
}

fun Candy.toIcon(): String = when (this) {
    is Red -> "🍅"
    is Orange -> "🍌"
    is Yellow -> "🍋"
    is Green -> "🍒"
    is Blue -> "🍍"
    is Purple -> "🍓"
    is ColourBomb -> "🍪"
    is HorStriped -> "🢐${candy.toIcon()} 🢒"
    is VerStriped -> "🢓${candy.toIcon()} 🢑"
}

tailrec fun Candy.ansiColor(): String = when (this) {
    is Red -> Color.ANSI_RED
    is Orange -> Color.ANSI_YELLOW
    is Yellow -> Color.ANSI_GREEN
    is Green -> Color.ANSI_CYAN
    is Blue -> Color.ANSI_BLUE
    is Purple -> Color.ANSI_PURPLE
    is HorStriped -> candy.ansiColor()
    is VerStriped -> candy.ansiColor()
    else -> ""
}

fun KindedCandy.kind(): RegularCandy = when (this) {
    is HorStriped -> candy
    is VerStriped -> candy
    is RegularCandy -> this
}

fun RegularCandy.stripe(dir: Dir): StripedCandy = when (dir) {
    Up -> VerStriped(this)
    Down -> VerStriped(this)
    Left -> HorStriped(this)
    Right -> HorStriped(this)
}

fun RegularCandy.toRegularCandy(i: Int): RegularCandy = when (Math.abs(i % 6)) {
    0 -> Red
    1 -> Orange
    2 -> Yellow
    3 -> Green
    4 -> Blue
    5 -> Purple
    else -> throw IllegalStateException("Partial function: $i cannot be converted to regular candy3")
}

sealed class Dir
object Up : Dir()
object Down : Dir()
object Left : Dir()
object Right : Dir()

@isos data class Pos(val i: Int, val j: Int) {
    companion object { }

    private fun move(dir: Dir): Pos = when (dir) {
        Up -> copy(i = i - 1)
        Down -> copy(i = i + 1)
        Left -> copy(j = j - 1)
        Right -> copy(j = j + 1)
    }

    val down: Pos by lazy { move(Down) }
    val up: Pos by lazy { move(Up) }
    val left: Pos by lazy { move(Left) }
    val right: Pos by lazy { move(Right) }
}

@instance(Pos::class)
interface PosOrderInstance : Order<Pos> {

    fun OT(): Order<Tuple2<Int, Int>>

    override fun compare(a: Pos, b: Pos): Int =
            OT().compare(posIso().get(a), posIso().get(b))
}

sealed class SwitchOut
object NotPlaying : SwitchOut()
object InvalidMove : SwitchOut()
object YouLose : SwitchOut()
object YouWin : SwitchOut()
object Ok : SwitchOut()