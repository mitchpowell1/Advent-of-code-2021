package day17

import utils.CollectionExtensions.product
import utils.FileUtil
import kotlin.system.measureNanoTime
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun main() {
    val (xs, ys) = """.*x=(-?\d+..-?\d+).*y=(-?\d+..-?\d+)""".toRegex()
        .find(FileUtil.readFileToString("./src/day17/input.txt"))!!.destructured
    val (x1, x2) = xs.split("..").map { it.toInt() }
    val (y1, y2) = ys.split("..").map { it.toInt() }
    repeat (10) {
        println(calcStep(it, Pair(7,2)))
    }
    val compTime = measureNanoTime { wompTargetWithStyle(Target(x1..x2, y1..y2)) }
    println(Duration.nanoseconds(compTime))
}

fun sumTo(n: Int) : Int = (n * (n + 1)) / 2

fun calcStep(step: Int, vector: Pair<Int, Int>) = Pair(
    if (step >= vector.first) sumTo(vector.first) else step * (2 * vector.first - step + 1) / 2,
    step * (2 * vector.second - step + 1) / 2
)

fun wompTargetWithStyle(target: Target) {
    val successfulResults = (0..target.rightWall).product(target.bottomWall..200)
        .map { (x,y) ->
            simulate(Pair(x,y), target)
        }
        .filter {
            it.hit
        }
    println(successfulResults.maxOf { it.maxY })
    println(successfulResults.toList().size)
}

fun simulate(initialVelocity: Pair<Int,Int>, target: Target): SimulationResult {
    var next = initialVelocity
    var i = 0
    var maxY = Int.MIN_VALUE
    while(!(target isOverShotBy next)) {
        next = calcStep(i, initialVelocity)
        if (next.second > maxY) {
            maxY = next.second
        }
        if (target isHitBy next) {
            return SimulationResult(true, maxY)
        }
        i++
    }
    return SimulationResult(false, maxY)
}

data class SimulationResult(val hit: Boolean, val maxY: Int)

class Target(private val xRange: IntRange, private val yRange: IntRange) {
    val bottomWall = yRange.first
    val rightWall = xRange.last

    infix fun isHitBy(coordinates: Pair<Int,Int>) = coordinates.first in xRange && coordinates.second in yRange

    infix fun isOverShotBy(coordinates: Pair<Int,Int>) = this isLeftOf coordinates || this isAbove coordinates

    private infix fun isLeftOf(coordinates: Pair<Int, Int>) = coordinates.first > rightWall

    private infix fun isAbove(coordinates: Pair<Int, Int>) = coordinates.second < bottomWall
}
