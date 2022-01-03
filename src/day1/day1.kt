package day1

import utils.FileUtil

fun computeDepthIncreases(values: List<Int>, windowSize: Int = 1) = values
    .windowed(windowSize,1) { it.sum() }
    .zipWithNext()
    .count { (a, b) -> a < b }

fun main() {
    val input = FileUtil.readFileAsInts("src/day1/input1.txt")

    println(computeDepthIncreases(input))
    println(computeDepthIncreases(input,3))
}
