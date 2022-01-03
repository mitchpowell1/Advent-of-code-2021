package day25

import utils.CollectionExtensions.toGrid
import utils.FileUtil
import utils.Grid

fun main() {
    val input = FileUtil.readFileAsStrings("./src/day25/input.txt")
    val cukes = input.flatMapIndexed { rowIndex, row ->
        row.mapIndexedNotNull { colIndex, c ->
            if (c == '.') {
                null
            } else {
                SeaCucumber(Direction.fromChar(c), Pair(rowIndex, colIndex))
            }
        }
    }

    val grid = input.map { r -> r.map { it != '.' }.toMutableList() }.toGrid()

    val sim = Simulation(cukes, grid)
    println(sol1(sim))
}

fun sol1(sim: Simulation): Int {
    var steps = 0
    var solved = false
    while (!solved) {
        solved = sim.step()
        steps += 1
    }
    return steps
}

enum class Direction {
    EAST,
    SOUTH;

    companion object {
        fun fromChar(char: Char) = when(char) {
            '>' -> EAST
            'v' -> SOUTH
            else -> error("Invalid direction char: $char")
        }
    }
}

data class SeaCucumber(val direction: Direction, var coordinates: Pair<Int,Int>)

class Simulation(val cucumbers: List<SeaCucumber>, val grid: Grid<Boolean>) {
    fun step(): Boolean {
        val gridCopy1 = grid.map { false }
        val gridCopy2 = grid.map { false }
        val movedEasternSeaCucumbers = mutableSetOf<Pair<Int,Int>>()
        cucumbers
            .filter { it.direction == Direction.EAST }
            .forEach { cuke ->
                val nextPosition = Pair(cuke.coordinates.first, (cuke.coordinates.second + 1) % grid.width)
                if (!grid[nextPosition]) {
                    gridCopy1[nextPosition] = true
                    movedEasternSeaCucumbers.add(cuke.coordinates)
                    cuke.coordinates = nextPosition
                } else {
                    gridCopy1[cuke.coordinates] = true
                }
            }
        movedEasternSeaCucumbers.forEach { grid[it] = false }
        var southCukeHasMoved = false
        cucumbers
            .filter { it.direction == Direction.SOUTH }
            .forEach { cuke ->
                val nextPosition = Pair((cuke.coordinates.first + 1) % grid.height, cuke.coordinates.second)
                if (!(grid[nextPosition] || gridCopy1[nextPosition])) {
                    southCukeHasMoved = true
                    gridCopy2[nextPosition] = true
                    cuke.coordinates = nextPosition
                } else {
                    gridCopy2[cuke.coordinates] = true
                }
            }
        grid.getAllCoordinates().forEach {
            grid[it] = gridCopy1[it] || gridCopy2[it]
        }
        return !southCukeHasMoved && movedEasternSeaCucumbers.isEmpty()
    }
}