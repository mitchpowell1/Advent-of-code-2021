package day12

import utils.FileUtil

private const val START_NAME = "start"
private const val END_NAME = "end"

fun main() {
    val input = FileUtil.readLinesWithTransform("./src/day12/input.txt") {
        val (cave1, cave2) = it.split('-')
        CaveInput(cave1, cave2)
    }
    val caveNetwork = constructCaveNetwork(input)
    println(countPaths(caveNetwork))
    println(countPaths(caveNetwork, allowRevisit = true))
}

fun constructCaveNetwork(inputs: List<CaveInput>) : Cave {
    val caves: MutableSet<Cave> = inputs
        .map { listOf(Cave(it.cave1), Cave(it.cave2)) }
        .flatten()
        .toMutableSet()

    inputs.forEach { caveInput ->
        val cave1 = caves.first { it.name == caveInput.cave1 }
        val cave2 = caves.first { it.name == caveInput.cave2 }
        cave1.addConnection(cave2)
        cave2.addConnection(cave1)
    }

    return caves.first { it.name == START_NAME }
}

fun countPaths(startCave: Cave, allowRevisit: Boolean = false): Int {
    fun helper(cave: Cave, visited: Set<Cave>, doubleDipped: Boolean): Int {
        val doubleDipping = doubleDipped || cave in visited && cave.isSmall
        val newVisited: MutableSet<Cave> = mutableSetOf(cave).apply { addAll(visited) }
        if (cave.name == END_NAME) {
            return 1
        }
        return cave.connections
            .filterNot { it.isSmall && doubleDipping && it in visited }
            .sumOf { helper(it, newVisited, doubleDipping) }
    }

    return helper(startCave, setOf(), !allowRevisit)
}

data class CaveInput(val cave1: String, val cave2: String)

class Cave(val name: String) {
    val isSmall = name != name.uppercase()

    private val _connections: MutableList<Cave> = mutableListOf()
    val connections: List<Cave> = _connections

    fun addConnection(cave: Cave) {
        if (cave.name != START_NAME && this.name != END_NAME) {
            _connections.add(cave)
        }
    }
}