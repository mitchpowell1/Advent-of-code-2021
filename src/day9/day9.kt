package day9

import utils.CollectionExtensions.product
import utils.CollectionExtensions.toGrid

import utils.FileUtil
import utils.Grid

import java.util.LinkedList
import java.util.Queue

typealias Point = Pair<Int,Int>
fun main() {
    val input: Grid<Int> = FileUtil
        .readLinesWithTransform("./src/day9/input.txt") { it.map(Char::digitToInt).toMutableList() }
        .toGrid()

    println(sol1(input))
    println(sol2(input))
}

fun getLowPoints(heatMap: Grid<Int>) = heatMap
    .getAllCoordinates()
    .filter { coords ->
        heatMap.getAdjacent(coords).all { it > heatMap[coords] }
    }

fun mapBasin(startingPoint: Point, heatMap: Grid<Int>): Set<Point> {
    val explored: MutableSet<Point> = mutableSetOf()
    val toExplore: Queue<Point> = LinkedList<Point>().apply { add(startingPoint) }

    while (toExplore.isNotEmpty()) {
        val nextPoint = toExplore.poll()
        val pointValue = heatMap[nextPoint]
        if (pointValue != 9) {
            toExplore.addAll(heatMap.getAdjacentPairs(nextPoint).filter { !explored.contains(it) })
            explored.add(nextPoint)
        }
    }
    return explored
}

fun sol1(heatMap: Grid<Int>) = getLowPoints(heatMap).sumOf { heatMap[it] + 1 }

fun sol2(heatMap: Grid<Int>): Int {
    val exploredPoints: MutableSet<Point> = mutableSetOf()
    val basins: MutableList<Set<Point>> = mutableListOf()
    val lowPoints = getLowPoints(heatMap)

    lowPoints.forEach {
        if (!exploredPoints.contains(it)) {
            val basin = mapBasin(it, heatMap)
            exploredPoints.addAll(basin)
            basins.add(basin)
        }
    }

    return basins
        .sortedByDescending { it.size }
        .take(3)
        .fold(1) { acc, pairs ->  acc * pairs.size }
}