package utils

import utils.CollectionExtensions.product
import java.lang.Integer.max
import java.lang.Integer.min

class Grid<T>(private val data: List<MutableList<T>>): Iterable<List<T>> by data {
    enum class Direction { UP, DOWN, LEFT, RIGHT}
    fun <R>map(transform: (T) -> R): Grid<R> {
        return Grid(data.map { row -> row.map { transform(it) }.toMutableList()})
    }

    val width: Int
        get() {
            return data[0].size
        }

    val height: Int
        get() {
            return data.size
        }

    val rows: List<List<T>>
        get() {
            return data
        }

    val columns: List<List<T>>
        get() {
            return (0 until width).map { c ->
                (0 until height).map { r ->
                    data[r][c]
                }
            }
        }

    fun getNeighborCoordinates(coords: Pair<Int, Int>, direction: Direction) =
        getNeighborCoordinates(coords.first, coords.second, direction)

    fun getNeighborCoordinates(row: Int, col: Int, direction: Direction) = when (direction) {
        Direction.UP -> if (row == 0) null else Pair(row-1,col)
        Direction.DOWN -> if (row == height - 1) null else Pair(row+1,col)
        Direction.LEFT -> if (col == 0) null else Pair(row,col-1)
        Direction.RIGHT -> if (col == width - 1) null else Pair(row,col+1)
    }

    fun getColumn(index: Int) = data.map { it[index] }

    fun getAdjacentPairs(coordinatePair: Pair<Int, Int>, includeDiagonals: Boolean = false) =
        getAdjacentPairs(coordinatePair.first, coordinatePair.second, includeDiagonals)

    fun getAdjacentPairs(row: Int, col: Int, includeDiagonals: Boolean = false): Sequence<Pair<Int, Int>> = sequence {
        val rowOffsets = (max(row - 1, 0)..min(row + 1, height - 1))
        val colOffsets = (max(col - 1, 0)..min(col + 1, width - 1))
        rowOffsets.product(colOffsets).forEach {
            if (it.first != row && it.second != col ) {
                if (includeDiagonals) yield(it)
            } else {
                if (it.first != row || it.second != col) yield(it)
            }
        }
    }

    fun getAdjacent(row: Int, col: Int, includeDiagonals: Boolean = false): Sequence<T> = sequence {
        getAdjacentPairs(row, col, includeDiagonals).forEach { yield(get(it)) }
    }

    fun getAdjacent(coords: Pair<Int, Int>, includeDiagonals: Boolean = false): Sequence<T> = sequence {
        getAdjacentPairs(coords.first, coords.second, includeDiagonals).forEach { yield(get(it)) }
    }

    fun getAll() = sequence {
        data.flatten().forEach { yield(it) }
    }

    fun getAllCoordinates() = sequence {
        (0 until height).product(0 until width).forEach { yield(it) }
    }
    fun getAllCoordinatesReversed() = sequence {
        (height - 1 downTo 0).product(width - 1 downTo 0).forEach { yield(it) }
    }

    operator fun get(index: Int): List<T> = data[index]
    operator fun get(rowColumnPair: Pair<Int, Int>) = data[rowColumnPair.first][rowColumnPair.second]
    operator fun set(coords: Pair<Int, Int>, value: T) { data[coords.first][coords.second] = value}
    operator fun set(r: Int, c: Int, value: T) { data[r][c] = value}
}