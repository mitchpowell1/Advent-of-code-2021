package day13

import utils.CollectionExtensions.product
import utils.FileUtil
import kotlin.math.absoluteValue

enum class Axis { X, Y }
data class Fold(val axis: Axis, val value: Int)

fun parsePoints(input: String): List<Pair<Int, Int>> = input.lines()
    .map {
        val (x, y) = it.split(',')
        Pair(x.toInt(), y.toInt())
    }

fun parseFolds(input: String) = input.lines()
    .map {
        val (instruction, value) = it.split('=')
        Fold(axis = Axis.valueOf(instruction.last().uppercase()), value = value.toInt())
    }

fun main() {
    val (points, folds) = FileUtil.readFileToString("./src/day13/input.txt").split("\n{2,}".toRegex())
    val parsedPoints = parsePoints(points)
    val parsedFolds = parseFolds(folds)
    println(foldPoints(parsedPoints, parsedFolds, testFold = true).size)
    printLetters(parsedPoints, parsedFolds)
}

fun getReflection(pointVal: Int, axis: Int) = axis - (pointVal - axis).absoluteValue

fun foldPoints(points: List<Pair<Int,Int>>, folds: List<Fold>, testFold: Boolean = false): Set<Pair<Int,Int>> {
    var pointSet = points.toSet()
    val numFolds = if (testFold) 1 else folds.size
    folds
        .take(numFolds)
        .forEach { (axis, value) ->
            pointSet = when (axis) {
                Axis.X -> pointSet.map { (x, y) -> Pair(getReflection(x, value), y) }
                Axis.Y -> pointSet.map { (x, y) -> Pair(x, getReflection(y, value)) }
            }.toSet()
        }
    return pointSet
}

fun printLetters(points: List<Pair<Int,Int>>, folds: List<Fold>) {
    val foldedPoints = foldPoints(points, folds)
    val maxX = foldedPoints.maxOf { (x,_) -> x }
    val maxY = foldedPoints.maxOf { (_,y) -> y }

    (0..maxY).product(0..maxX).forEach { (y, x) ->
        print(if (Pair(x,y) in foldedPoints) "#" else " ")
        if (x == maxX) println()
    }
}