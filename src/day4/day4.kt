package day4

import utils.CollectionExtensions.product
import utils.FileUtil

private const val BOARD_WIDTH = 5
private const val BOARD_HEIGHT = 5

fun main() {
    val rawInput = FileUtil.readFileToString("src/day4/input.txt").split("\n\n")
    val numbers = rawInput[0].split(',').map(String::toInt)
    val boards = rawInput
        .subList(1, rawInput.size)
        .map{BingoBoard(it)}
    println(sol1(boards, numbers))
    println(sol2(boards, numbers))
}

fun sol1(boards: List<BingoBoard>, numbers: List<Int>) : Int {
    numbers.product(boards).forEach { (num, board) ->
        if (board.mark(num)) {
            return board.score() * num
        }
    }
    return -1
}

fun sol2(boards: List<BingoBoard>, numbers: List<Int>) : Int {
    var uncompletedBoards: MutableList<BingoBoard> = ArrayList(boards)
    numbers.forEach { num ->
        val toRemove: MutableList<BingoBoard> = mutableListOf()
        uncompletedBoards.forEach {
            if (it.mark(num)) {
                if (uncompletedBoards.size == 1) {
                    return num * it.score()
                }
                toRemove.add(it)
            }
        }
        toRemove.forEach(uncompletedBoards::remove)
    }
    return -1
}

class BingoBoard(boardInput: String) {
    private val values: List<Square> =  boardInput
        .split("""\s+""".toRegex())
        .filter(String::isNotEmpty)
        .map { Square(it) }

    operator fun get(index: Int): List<Square> {
        val startIndex = index * BOARD_HEIGHT
        return values.subList(startIndex, startIndex + BOARD_WIDTH)
    }

    private fun getColumn(index: Int): List<Square> = values
        .windowed(BOARD_WIDTH, BOARD_WIDTH)
        .map { it[index] }

    fun mark(value: Int) : Boolean {
        this.values.forEachIndexed { i, cell ->
            if (cell.value == value) {
                cell.state = SquareState.MARKED
                return check(row = i / BOARD_HEIGHT, col = i % BOARD_WIDTH)
            }
        }
        return false
    }

    private fun check(row: Int, col: Int) = when {
        // Row is all marked
        this[row].all { it.state == SquareState.MARKED } -> true
        // Column is all marked
        getColumn(col).all { it.state == SquareState.MARKED } -> true
        else -> false
    }

    fun score() = this.values
        .filter { it.state == SquareState.UNMARKED }
        .sumOf { it.value }
}

enum class SquareState {
    MARKED,
    UNMARKED,
}

data class Square(var value: Int, var state: SquareState) {
    constructor (value: String) : this(value.toInt(), SquareState.UNMARKED)
}
