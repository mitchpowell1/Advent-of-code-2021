package day7

import utils.FileUtil
import java.util.Collections.max
import java.util.Collections.min
import kotlin.math.absoluteValue

fun main() {
    val rawInput = FileUtil.readFileToString("src/day7/sample_input.txt")
        .split(',')
        .map(String::toInt)
    println(calculateIdealFuel(rawInput) { it })
    println(calculateIdealFuel(rawInput) { n -> (n * (n + 1)) / 2 })
}

fun calculateIdealFuel(crabPositions: List<Int>, fuelFunction: (Int) -> Int): Int {
    val frequencies = crabPositions.groupingBy { it }.eachCount()
    return (min(crabPositions)..max(crabPositions)).minOf { offset ->
        frequencies.entries.sumOf {(pos,numCrabs) ->
            numCrabs * fuelFunction((offset - pos).absoluteValue)
        }
    }
}