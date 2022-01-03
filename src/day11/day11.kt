package day11

import utils.CollectionExtensions.toGrid
import utils.FileUtil
import utils.Grid
import java.util.*

typealias Point = Pair<Int,Int>

fun main() {
    val input = FileUtil.readLinesWithTransform("./src/day11/input.txt") { line ->
        line.map { it.digitToInt() }.toMutableList()
    }
    println(sol1(input.toGrid().map { Octopus(it) }))
    println(sol2(input.toGrid().map {  Octopus(it) }))
}

fun sol1(octopuses: Grid<Octopus>) : Int {
    val simulation = Simulation(octopuses)
    return (0 until 100).sumOf { simulation.step() }
}

fun sol2(octopuses: Grid<Octopus>) : Int {
    val simulation = Simulation(octopuses)
    var steps = 0
    var result = -1
    while(result != octopuses.width * octopuses.height) {
        steps += 1
        result = simulation.step()
    }
    return steps
}

class Simulation(private val octopuses: Grid<Octopus>) {
    fun step() : Int {
        // Phase 1
        octopuses.getAll().forEach { it.energyLevel++ }
        // Phase 2
        val toFlash: Queue<Point> = LinkedList<Point>().apply {
            addAll(octopuses.getAllCoordinates().filter { octopuses[it].energyLevel > 9 })
        }
        val flashed: MutableSet<Point> = mutableSetOf()
        while (toFlash.isNotEmpty()) {
            val next = toFlash.poll()
            flashed.add(next)
            octopuses.getAdjacentPairs(next, includeDiagonals = true).forEach {
                val octopus = octopuses[it]
                octopus.energyLevel++
                if (octopus.energyLevel > 9 && it !in flashed && it !in toFlash) {
                    toFlash.add(it)
                }
            }
        }
        flashed.forEach { octopuses[it].energyLevel = 0}
        return flashed.size
    }
}

data class Octopus(var energyLevel: Int)