package day8

import utils.FileUtil

enum class Segment { A, B, C, D, E, F, G }

data class InOut(val inputs: SegmentSet, val outputs: SegmentSet)
typealias SegmentSet = List<Set<Segment>>

val digitSegments = listOf(
    setOf("A", "B", "C", "E", "F", "G"), // 0
    setOf("C", "F"), // 1
    setOf("A", "C", "D", "E", "G"), // 2
    setOf("A", "C", "D", "F", "G"), // 3
    setOf("B", "C", "D", "F"), // 4
    setOf("A", "B", "D", "F", "G"), // 5
    setOf("A", "B", "D", "E", "F", "G"), // 6
    setOf("A", "C", "F"), // 7
    setOf("A", "B", "C", "D", "E", "F", "G"), // 8
    setOf("A", "B", "C", "D", "F", "G"), // 9
).map { digit -> digit.map { Segment.valueOf(it) }.toSet() }

fun convertStringToSetOfSegments(inStr: String) = inStr.map { Segment.valueOf(it.uppercase()) }.toSet()
fun countOccurrences(segment: Segment, inSets: List<Set<Segment>>) = inSets.count { segment in it }

fun getSegmentWithNOccurrences(segments: Collection<Segment>, sets: List<Set<Segment>>, occurrences: Int) = segments
    .first { countOccurrences(it, sets) == occurrences }

fun main() {
    val input = FileUtil.readLinesWithTransform("src/day8/input.txt") {
        val (segmentInputs, segmentOutputs) = it.split("|").map { str -> str.trim().split(" ") }
        InOut(
            segmentInputs.map(::convertStringToSetOfSegments),
            segmentOutputs.map(::convertStringToSetOfSegments)
        )

    }
    println(sol1(input))
    println(sol2(input))
}

fun sol1(inOuts: List<InOut>) = inOuts.sumOf { it.outputs.count { output -> output.size in setOf(2, 3, 4, 7) } }

fun sol2(inOuts: List<InOut>): Int {
    return inOuts.sumOf { inOut ->
        val wireMap = mapWires(inOut.inputs)
        inOut.outputs
            .map { output -> digitSegments.indexOf(output.map { wireMap[it] }.toSet()) }
            .reduce { acc, next -> acc * 10 + next }
    }
}

fun mapWires(inputs: SegmentSet): Map<Segment, Segment> {
    val wireMapping: MutableMap<Segment, Segment> = mutableMapOf()
    val sortedInputs = inputs.sortedBy { it.size }

    wireMapping[(sortedInputs[1] subtract sortedInputs[2]).first()] = Segment.A

    wireMapping[
        listOf(sortedInputs[2], sortedInputs[6], sortedInputs[7], sortedInputs[8])
            .reduce(Set<Segment>::intersect)
            .subtract(sortedInputs[0])
            .first()
    ] = Segment.B

    wireMapping[(sortedInputs[2] subtract sortedInputs[0] subtract wireMapping.keys.toSet()).first()] = Segment.D

    wireMapping[getSegmentWithNOccurrences(
        sortedInputs[0],
        listOf(sortedInputs[6], sortedInputs[7], sortedInputs[8]),
        3
    )] = Segment.F

    wireMapping[(sortedInputs[0] subtract wireMapping.keys.toSet()).first()] = Segment.C

    wireMapping[getSegmentWithNOccurrences(
        Segment.values() subtract wireMapping.keys.toSet(),
        listOf(sortedInputs[3], sortedInputs[4], sortedInputs[5]),
        3
    )] = Segment.G

    wireMapping[(Segment.values() subtract wireMapping.keys.toSet()).first()] = Segment.E

    return wireMapping
}
