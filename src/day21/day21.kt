package day21

import utils.FileUtil

const val WIN_CONDITION_PT_1 = 1000
const val WIN_CONDITION_PT_2 = 21

data class Player(var position: Int, var score: Int)

data class BoardState(
    val turn: Int,
    val p0Score: Int,
    val p0Position: Int,
    val p1Score: Int,
    val p1Position: Int,
) {
    val boardStateID: String
    get() {
        return "$turn|$p0Position|$p0Score|$p1Position|$p1Score"
    }
}

object DeterministicDie {
    var value = 0
    fun roll(): Int {
        value += 1
        return value
    }
}

val quantumRollWeights = mapOf(
    3 to 1,
    4 to 3,
    5 to 6,
    6 to 7,
    7 to 6,
    8 to 3,
    9 to 1,
)

private fun main() {
    val players = FileUtil.readLinesWithTransform("./src/day21/input.txt") {
        val ( initialPosition ) = "(\\d+)$".toRegex().find(it)!!.destructured
        Player(initialPosition.toInt(), 0)
    }

    println(sol1(players.map(Player::copy)))
    println(sol2(players.map(Player::copy)))
}

private fun sol1(players: List<Player>): Int {
    var rollingPlayer = 0
    while (players.none { it.score >= WIN_CONDITION_PT_1 }) {
        val player = players[rollingPlayer]
        val playerPositionDelta = (0 until 3).sumOf { DeterministicDie.roll() }
        player.position = computePosition(player.position, playerPositionDelta)
        player.score += player.position
        rollingPlayer = rollingPlayer xor 1
    }
    return players[rollingPlayer].score * DeterministicDie.value
}

private fun sol2(players: List<Player>): Long {
    val memo: MutableMap<String, Result> = mutableMapOf()
    fun helper(boardState: BoardState): Result {
        if (boardState.boardStateID in memo) {
            return memo[boardState.boardStateID]!!
        }
        var p0Victories = 0L
        var p1Victories = 0L
        (3..9).forEach { roll ->
            val rollWeight = quantumRollWeights[roll]!!
            val nextState = computeNextState(boardState, roll)
            if (boardState.turn == 0 && nextState.p0Score >= WIN_CONDITION_PT_2) {
                p0Victories += rollWeight
            } else if (boardState.turn == 1 && nextState.p1Score >= WIN_CONDITION_PT_2) {
                p1Victories += rollWeight
            } else {
                val nextStateResult = helper(nextState)
                p0Victories += nextStateResult.p0Victories * rollWeight
                p1Victories += nextStateResult.p1Victories * rollWeight
            }
        }
        val result = Result(p0Victories, p1Victories)
        memo[boardState.boardStateID] = result
        return result
    }
    val result = helper(
        BoardState(
            turn = 0,
            p0Score = 0,
            p0Position = players[0].position,
            p1Score = 0,
            p1Position = players[1].position
        )
    )

    return maxOf(result.p0Victories, result.p1Victories)
}

data class Result(val p0Victories: Long, val p1Victories: Long)

fun computeNextState(boardState: BoardState, rollValue: Int): BoardState {
    val (turn, p0Score, p0Position, p1Score, p1Position) = boardState
    val movingPlayerScore = if (turn == 0) p0Score else p1Score
    val movingPlayerPosition = if (turn == 0) p0Position else p1Position
    val nextTurn = turn xor 1

    val nextPosition = computePosition(movingPlayerPosition, rollValue)
    val nextScore = movingPlayerScore + nextPosition

    return BoardState(
        turn = nextTurn,
        p0Score = if (turn == 0) nextScore else p0Score,
        p0Position = if (turn == 0) nextPosition else p0Position,
        p1Score = if (turn == 1) nextScore else p1Score,
        p1Position = if (turn == 1) nextPosition else p1Position,
    )
}

fun computePosition(current: Int, delta: Int) = (current + delta - 1) % 10 + 1