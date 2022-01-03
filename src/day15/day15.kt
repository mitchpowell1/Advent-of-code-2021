package day15

import utils.CollectionExtensions.product
import utils.CollectionExtensions.toGrid
import utils.FileUtil
import utils.Grid
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.*
import kotlin.system.measureNanoTime
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun main() {
    val input = FileUtil.readLinesWithTransform("./src/day15/input.txt") {
        it.map { char -> char.digitToInt() }.toMutableList()
    }.toGrid()

    val p2Time = measureNanoTime { println(aStar(input)) }
    println("Part 2 time: ${Duration.nanoseconds(p2Time)}")
}

fun getInputValue(input: Grid<Int>, r: Int, c: Int): Int {
    val tf = r / input.height + c / input.width
    return (input[r % input.height][c % input.width] + tf - 1) % 9 + 1
}

fun aStar(input: Grid<Int>) : Int {
    val widthBoundary = input.width * 5
    val heightBoundary = input.height * 5
    val distances = (0 until heightBoundary).product(0 until widthBoundary).associate {
        it.pointId() to if (it == Pair(0,0)) 0 else Int.MAX_VALUE
    }.toMutableMap()
    val toVisit: Queue<Pair<Int,Int>> = PriorityQueue(
        distances.entries.size
    ) { a, b -> distances[a.pointId()]!!.compareTo(distances[b.pointId()]!!) }
    toVisit.add(Pair(0,0))
    while (toVisit.isNotEmpty()) {
        val nextVisit = toVisit.poll()
        toVisit.remove(nextVisit)
        getAdjacentPairs(heightBoundary, widthBoundary, nextVisit).forEach {
            val newDistance = distances[nextVisit.pointId()]!! + getInputValue(input, it.first, it.second)
            if (distances[it.pointId()]!! > newDistance) {
                distances[it.pointId()] = newDistance
                toVisit.add(it)
            }
        }
        if (nextVisit == Pair(heightBoundary - 1, widthBoundary - 1)) {
            break
        }
    }
    return distances[Pair(heightBoundary - 1, widthBoundary - 1).pointId()]!!
}

fun getAdjacentPairs(heightBoundary: Int, widthBoundary: Int, coord: Pair<Int, Int>): List<Pair<Int,Int>> {
    val pairs: MutableList<Pair<Int,Int>> = mutableListOf()
    if (coord.first > 0) {
        pairs.add(Pair(coord.first - 1, coord.second))
    }

    if (coord.first < heightBoundary - 1) {
        pairs.add(Pair(coord.first + 1, coord.second))
    }

    if (coord.second < widthBoundary - 1) {
        pairs.add(Pair(coord.first, coord.second + 1))
    }

    if (coord.second > 0) {
        pairs.add(Pair(coord.first, coord.second - 1))
    }

    return pairs
}

fun Pair<Int,Int>.pointId() = "(${this.first}, ${this.second})"