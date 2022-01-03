package day22

import utils.FileUtil
import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.math.absoluteValue

const val INIT_PROC_LOWER = -50
const val INIT_PROC_UPPER = 50

data class RebootStep(
    val activate: Boolean,
    val cuboid: Cuboid
)

fun parseRange(rangeString: String) : IntRange {
    val (lower, upper) = """(-?\d+)\.\.(-?\d+)""".toRegex().find(rangeString)!!.destructured
    return (lower.toInt()..upper.toInt())
}

fun main() {
    val rebootSteps = FileUtil.readLinesWithTransform("./src/day22/input.txt") {
        val (action, ranges) = it.split(" ")
        val (xRange, yRange, zRange) = ranges.split(',')
        RebootStep(
            activate = action == "on",
            cuboid = Cuboid(
                parseRange(xRange),
                parseRange(yRange),
                parseRange(zRange),
            )
        )
    }
    println(countActivatedCubes(rebootSteps.filter { it.cuboid.boundedBy(INIT_PROC_LOWER..INIT_PROC_UPPER)}))
    println(countActivatedCubes(rebootSteps))
}

fun countActivatedCubes(rebootSteps: List<RebootStep>): Long {
    val alteredCubes: MutableSet<Cuboid> = mutableSetOf()
    rebootSteps.forEach { (activate, cuboid) ->
        val intersectingCubes = alteredCubes.filter { it intersectsWith cuboid }
        intersectingCubes.forEach {
            val newCuboids = it.computeDifferenceDecomposition(cuboid)
            newCuboids?.forEach { c -> alteredCubes.add(c) }
            alteredCubes.remove(it)
        }
        if (activate) {
            alteredCubes.add(cuboid)
        }
    }
    return alteredCubes.sumOf(Cuboid::volume)
}

/**
 * All methods in cuboid assume axial alignment
 */
class Cuboid(private val xRange: IntRange, private val yRange: IntRange, private val zRange: IntRange) {
    infix fun intersectsWith(other: Cuboid): Boolean {
        return xRange overlapsWith other.xRange && yRange overlapsWith other.yRange && zRange overlapsWith other.zRange
    }

    val volume: Long
    get() {
        return xRange.size.toLong() * yRange.size.toLong() * zRange.size.toLong()
    }

    fun computeDifferenceDecomposition(other: Cuboid): List<Cuboid>? {
        if (!(this intersectsWith other)) return null

        val intersectionCuboid = this.intersection(other)!!

        val xRanges = xRange difference intersectionCuboid.xRange
        val yRanges = yRange difference intersectionCuboid.yRange
        val zRanges = zRange difference intersectionCuboid.zRange

        val outputCuboids: MutableList<Cuboid> = mutableListOf()

        xRanges.toList().filterNotNull().forEach {
            outputCuboids.add(Cuboid(it, yRange, zRange))
        }

        yRanges.toList().filterNotNull().forEach {
            outputCuboids.add(Cuboid(intersectionCuboid.xRange, it, intersectionCuboid.zRange))
        }

        zRanges.toList().filterNotNull().forEach {
            outputCuboids.add(Cuboid(intersectionCuboid.xRange, yRange, it))
        }

        return outputCuboids
    }

    fun boundedBy(range: IntRange) = xRange in range && yRange in range && zRange in range

    fun intersection(other: Cuboid) = if (!(this intersectsWith other))
        null
    else
        Cuboid(
            other.xRange.overlap(xRange)!!,
            other.yRange.overlap(yRange)!!,
            other.zRange.overlap(zRange)!!,
        )
}

operator fun IntRange.contains(other: IntRange) = other.first in this && other.last in this

infix fun IntRange.overlapsWith(other: IntRange) = other.first in this || first in other

fun IntRange.overlap(other: IntRange) = if (!(this overlapsWith other))
    null
else
    max(other.first, first)..min(other.last, last)

val IntRange.size: Int
get(){
    return (last - first).absoluteValue + 1
}

infix fun IntRange.difference(other: IntRange): Pair<IntRange?, IntRange?> {
    return Pair(lowerDifference(other),upperDifference(other))
}

infix fun IntRange.lowerDifference(other: IntRange): IntRange? {
    if (other.first == first) return null

    return min(other.first, first)until max(other.first,first)
}

infix fun IntRange.upperDifference(other: IntRange): IntRange? {
    if (other.last == last) return null

    return min(other.last, last) + 1..max(other.last, last)
}
