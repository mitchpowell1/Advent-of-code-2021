package day14

import utils.CollectionExtensions.countByLong
import utils.FileUtil

typealias PolymerPair = Pair<Char, Char>

fun main() {
    val (polymerTemplate, insertionRules) = FileUtil.readFileToString("./src/day14/input.txt").split("\n{2,}".toRegex())
    val rules = insertionRules
        .lines()
        .associate {
            val (a,b, insertion) = """(\w)(\w) -> (\w)""".toRegex().find(it)?.destructured!!
            Pair(a.first(), b.first()) to insertion.first()
        }
    println(polymerize(polymerTemplate, rules))
    println(polymerize(polymerTemplate, rules, iterations = 40))
}

fun polymerize(template: String, rules: Map<PolymerPair, Char>, iterations: Int = 10): Long {
    var pairs = template.windowed(2).map { Pair(it[0], it[1]) }.countByLong()
    repeat(iterations) {
        pairs = pairs.entries
            .mapNotNull { (k, v) ->
                val insertion = rules[k]
                if (insertion == null) null else listOf(
                    InsertionRecord(Pair(k.first, insertion), v),
                    InsertionRecord(Pair(insertion, k.second), v)
                )
            }
            .flatten()
            .fold(mutableMapOf()) { acc, insertionRecord ->
                acc[insertionRecord.pair] = acc.getOrDefault(insertionRecord.pair, 0) + insertionRecord.number
                acc
            }
    }
    val counts = pairs.keys
        .flatMap { (first, second) -> listOf(first, second) }
        .toSet()
        .associateWith { base ->
            val rawSum = pairs.entries.sumOf { (k, v) ->
                if (base == k.first && base == k.second) {
                    2 * v
                } else if (base == k.first || base == k.second) {
                    v
                } else {
                    0
                }
            }
            if (rawSum % 2 == 0L) rawSum / 2 else (rawSum + 1) / 2
        }
    val maxPair = counts.maxOf { it.value }
    val minPair = counts.minOf { it.value }
    return maxPair - minPair
}

data class InsertionRecord(val pair: PolymerPair, val number: Long)