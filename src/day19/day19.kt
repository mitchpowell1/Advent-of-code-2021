package day19

import utils.CollectionExtensions.product
import utils.FileUtil
import java.util.*
import kotlin.math.*

fun main() {
    val scanners = FileUtil
        .readFileToString("./src/day19/input.txt")
        .split("""-+.*scanner.*-+""".toRegex())
        .map(String::trim)
        .filter(String::isNotEmpty).map {
            Scanner(it.lines().map { line ->
                val (first, second, third) = line.split(',')
                Triple(first.toInt(), second.toInt(), third.toInt())
            })
        }
    val relativePositions = getRelativePositions(scanners)
    println(computeDistinctBeaconCount(scanners, relativePositions))
    println(computeMaxManhattanDistance(scanners, relativePositions))
}

val thetas = listOf(0.0, PI, PI / 2, 3 * PI / 2)
val rotations = thetas.flatMap { x -> thetas.flatMap { y -> thetas.map { z -> Triple(x,y,z) }}}

data class Rotation(val points: List<Vector3D>, val rotation: Triple<Double,Double,Double>)
data class RelativePosition(
    val fromIndex: Int,
    val toIndex: Int,
    val rotation: Triple<Double,Double,Double>,
    val translation: Vector3D
)

fun getRelativePositions(scanners: List<Scanner>): List<RelativePosition> {
    val relativePositions: MutableList<RelativePosition> = mutableListOf()
    for (i in scanners.indices) {
        for (j in scanners.indices) {
            if (i == j) continue
            val sc1 = scanners[i]
            val sc2 = scanners[j]
            val firstScannerBeaconRotations = rotations.map { rotation ->
                Rotation(
                    sc1.relativeBeacons.map { it.rotate(rotation) },
                    rotation
                )
            }
            var maxOverlap = 0
            lateinit var maxOverlapRotation: VectorDouble3D
            lateinit var maxOverlapTranslation: Vector3D
            firstScannerBeaconRotations.forEach { rotation ->
                sc2.relativeBeacons.forEach { relativePoint ->
                    rotation.points.forEach { rotPoint ->
                        val translation =  relativePoint - rotPoint
                        val overlapSet = rotation.points
                            .filter { it + translation in sc2.relativeBeacons }
                            .map { it.undoRot(rotation.rotation)}
                        if (overlapSet.size > maxOverlap) {
                            maxOverlap = overlapSet.size
                            maxOverlapRotation = rotation.rotation
                            maxOverlapTranslation = translation
                        }
                    }
                }
            }
            if (maxOverlap >= 12) {
                relativePositions.add(
                    RelativePosition(
                        fromIndex = i,
                        toIndex = j,
                        rotation = maxOverlapRotation,
                        translation = maxOverlapTranslation
                    )
                )
            }
        }
    }
    return relativePositions
}

fun computeDistinctBeaconCount(scanners: List<Scanner>, relativePositions: List<RelativePosition>): Int {
    val transformationGraph = generateGraph(scanners, relativePositions)
    val transformedPoints = (scanners.indices).flatMap {
        val path = getPath(it, 0, transformationGraph)
        reduceByPath(scanners[it].relativeBeacons, path)
    }.toSet()
    return transformedPoints.size
}

fun computeMaxManhattanDistance(scanners: List<Scanner>, relativePositions: List<RelativePosition>): Int {
    val transformationGraph = generateGraph(scanners, relativePositions)
    val scannerPositions = (scanners.indices).map {
        val path = getPath(from = it, to = 0, graph = transformationGraph)
        getOverallTransform(path)
    }
    return scannerPositions.product(scannerPositions).maxOf { (a,b) -> a.manhattanDistance(b)}
}

fun getOverallTransform(path: List<Transform>): Vector3D {
    return path.fold(Vector3D(0,0,0)) { acc, transform ->
        acc.rotate(transform.rotation) + transform.translation
    }
}

fun reduceByPath(points: List<Vector3D>, path: List<Transform>): List<Vector3D> = points.map { point ->
    path.fold(point) { acc, transform ->
        acc.rotate(transform.rotation) + transform.translation
    }
}

fun getPath(from: Int, to: Int, graph: List<List<Transform?>>): List<Transform> {
    val toVisit: Queue<Pair<Int, List<Transform>>> = ArrayDeque()
    toVisit.add(Pair(from, listOf()))
    while (toVisit.isNotEmpty()) {
        val (node, path) = toVisit.poll()
        if (node == to) {
            return mutableListOf<Transform>().apply {
                addAll(path)
                add(graph[node][node]!!)
            }
        }
        graph[node].indices.forEach {
            if (graph[node][it] != null) {
                val newList: List<Transform> = mutableListOf<Transform>().apply {
                    addAll(path)
                    add(graph[node][it]!!)
                }
                toVisit.add(Pair(it, newList))
            }
        }
    }
    error("Bad input")
}

fun generateGraph(scanners: List<Scanner>, relativePositions: List<RelativePosition>): List<MutableList<Transform?>> {
    val adjacencies: List<MutableList<Transform?>> = scanners.map { scanners.map { null }.toMutableList() }
    scanners.indices.forEach {
        adjacencies[it][it] = Transform(
            translation = Vector3D(0,0,0),
            rotation = VectorDouble3D(0.0, 0.0, 0.0),
        )
    }
    relativePositions.forEach { (fromIndex, toIndex, rotation, translation) ->
        adjacencies[fromIndex][toIndex] = Transform(translation, rotation)
    }
    return adjacencies
}

data class Transform(val translation: Vector3D, val rotation: VectorDouble3D)

fun Vector3D.rotate(thetas: VectorDouble3D) = this
    .rotX(thetas.first)
    .rotY(thetas.second)
    .rotZ(thetas.third)

fun Vector3D.rotX(theta: Double) = this.multiply(
    listOf(
        listOf(1, 0, 0),
        listOf(0, cos(theta).roundToInt(), -sin(theta).roundToInt()),
        listOf(0, sin(theta).roundToInt(), cos(theta).roundToInt()),
    )
)

fun Vector3D.rotY(theta: Double) = this.multiply(
    listOf(
        listOf(cos(theta).roundToInt(), 0, sin(theta).roundToInt()),
        listOf(0, 1, 0),
        listOf(-sin(theta).roundToInt(), 0, cos(theta).roundToInt()),
    )
)

fun Vector3D.rotZ(theta: Double) = this.multiply(
    listOf(
        listOf(cos(theta).roundToInt(), -sin(theta).roundToInt(), 0),
        listOf(sin(theta).roundToInt(), cos(theta).roundToInt(), 0),
        listOf(0, 0, 1),
    )
)

fun Triple<Int,Int,Int>.multiply(rotationMatrix: List<List<Int>>) = Triple(
    first * rotationMatrix[0][0] + second * rotationMatrix[0][1] + third * rotationMatrix[0][2],
    first * rotationMatrix[1][0] + second * rotationMatrix[1][1] + third * rotationMatrix[1][2],
    first * rotationMatrix[2][0] + second * rotationMatrix[2][1] + third * rotationMatrix[2][2],
)


data class Scanner(val relativeBeacons: List<Triple<Int,Int,Int>>)

fun Vector3D.undoRot(rotationVector: VectorDouble3D) = this
    .rotX(-rotationVector.first)
    .rotY(-rotationVector.third)
    .rotZ(-rotationVector.second)

operator fun Vector3D.minus(other: Vector3D) = Vector3D(
    first - other.first,
   second - other.second,
    third - other.third,
)

operator fun Vector3D.plus(other: Vector3D) = Vector3D(
    first + other.first,
    second + other.second,
    third + other.third,
)

operator fun Vector3D.times(other: Int) = Vector3D(
    first * other,
    second * other,
    third * other,
)

operator fun VectorDouble3D.times(other: Double) = VectorDouble3D(
    first * other,
    second * other,
    third * other,
)

fun Vector3D.manhattanDistance(other: Vector3D): Int {
    return (first - other.first).absoluteValue + (second - other.second).absoluteValue + (third - other.third).absoluteValue
}

typealias Vector3D = Triple<Int,Int,Int>
typealias VectorDouble3D = Triple<Double, Double, Double>
