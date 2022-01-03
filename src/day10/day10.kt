package day10

import utils.FileUtil
import java.util.*

const val openers = "([{<"
val pairs = mapOf(
    ']' to '[',
    ')' to '(',
    '>' to '<',
    '}' to '{',
)

fun main() {
    val input = FileUtil.readFileAsStrings("./src/day10/input.txt")
    println(sol1(input))
    println(sol2(input))
}

fun isCorrupted(line: String): Triple<Boolean, Char?, ArrayDeque<Char>> {
    val stack = ArrayDeque<Char>()
    val badChar = line.firstOrNull {
        if (it in openers) {
            stack.push(it)
            false
        } else {
            pairs[it] != stack.pop()
        }
    }
    return Triple(badChar != null, badChar, stack)
}

fun sol1(lines: List<String>) : Int {
    val scores = mapOf(
        ')' to 3,
        ']' to 57,
        '}' to 1197,
        '>' to 25137,
    )
    return lines
        .mapNotNull { isCorrupted(it).second }
        .sumOf { scores[it]!! }
}

fun sol2(lines: List<String>): Long {
    val scores = mapOf(
        ')' to 1,
        ']' to 2,
        '}' to 3,
        '>' to 4,
    )
    val reversedPairs = pairs.entries.associate { (k,v) -> v to k }
    val lineScores = lines
        .map(::isCorrupted)
        .filter { !it.first }
        .map { (_, _, stack) ->
            var score = 0L
            while (stack.isNotEmpty()) {
                score *= 5
                score += scores[reversedPairs[stack.pop()]!!]!!
            }
            score
        }
        .sorted()

    return lineScores[lineScores.size / 2]
}
