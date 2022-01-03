package day2

import utils.FileUtil
import java.io.File

fun sol1(instructions: List<Instruction>) : Int {
    var horizontalPosition = 0
    var depth = 0
    instructions.forEach {
        when(it.direction) {
            Direction.DOWN -> depth += it.magnitude
            Direction.FORWARD -> horizontalPosition += it.magnitude
            Direction.UP -> depth -= it.magnitude
        }
    }

    return horizontalPosition * depth
}

fun sol2(instructions: List<Instruction>) : Int {
    var horizontalPosition = 0
    var depth = 0
    var aim = 0
    instructions.forEach {
        when(it.direction) {
            Direction.UP -> aim -= it.magnitude
            Direction.DOWN -> aim += it.magnitude
            Direction.FORWARD -> {
                depth += it.magnitude * aim
                horizontalPosition += it.magnitude
            }
        }
    }
    return horizontalPosition * depth
}

fun main() {
    val instructions = FileUtil.readLinesWithTransform("src/day2/input.txt") {
        val (direction, magnitude) = """([a-z]+)\s(\d+)""".toRegex().find(it)?.destructured ?: error("Bad input")
        Instruction(
            Direction.valueOf(direction.uppercase()),
            magnitude.toInt()
        )
    }
    println(sol1(instructions))
    println(sol2(instructions))
}

enum class Direction {
    FORWARD,
    UP,
    DOWN
}

data class Instruction(
    val direction: Direction,
    val magnitude: Int,
)
