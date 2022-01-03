package day5

import utils.FileUtil
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

fun main() {
    var lineSegments = FileUtil
        .readLinesWithTransform("src/day5/input.txt") {
            var (rawXs, rawYs) = """(\d+,\d+).*?(\d+,\d+)""".toRegex().find(it)!!.destructured
            var (x1, y1) = rawXs.split(',').map(String::toInt)
            var (x2, y2) = rawYs.split(',').map(String::toInt)
            LineSegment(Pair(x1, y1), Pair(x2, y2))
        }
    println(sol1(lineSegments))
    println(sol1(lineSegments, false))
}

fun sol1(lineSegments: List<LineSegment>, filterOutDiagonals: Boolean = true): Int {
    val pointCount: MutableMap<Pair<Int, Int>, Int> = mutableMapOf()
    lineSegments
        .filter { if (!filterOutDiagonals) true else it.slope == Float.POSITIVE_INFINITY || it.slope == 0f }
        .flatMap { it.getAllPoints() }
        .forEach { pointCount[it] = pointCount.getOrDefault(it, 0) + 1 }

    return pointCount.values.count { it >= 2 }
}

class LineSegment(val p1: Pair<Int, Int>, val p2: Pair<Int, Int>) {
    val slope: Float = when {
        p1.first == p2.first -> Float.POSITIVE_INFINITY
        else -> (p2.second - p1.second) / (p2.first - p1.first).toFloat()
    }

    fun getAllPoints() : List<Pair<Int,Int>> {
        val dX = p2.first - p1.first
        val dY = p2.second - p1.second
        val numSteps = max(abs(dX), abs(dY))
        return (0 until numSteps).scan(p1) { (x,y), _ ->
            Pair(x + dX.sign, y + dY.sign)
        }
    }
}