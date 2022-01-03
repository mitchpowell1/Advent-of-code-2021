package day24

import utils.FileUtil
import kotlin.system.measureTimeMillis
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
private fun main() {
    val instructions = FileUtil.readLinesWithTransform("./src/day24/input.txt") {
        val instruction = it.split(" ")
        Instruction(InstructionType.valueOf(instruction[0].uppercase()), instruction[1], instruction.getOrNull(2))
    }

    val executionTime = measureTimeMillis {
        val processedInstructions = processInstructions(instructions)
        println("Max Number: ${sol1(processedInstructions)}")
        println("Min Number: ${sol2(processedInstructions)}")
    }

    println("Total execution time: ${Duration.milliseconds(executionTime)}")
}

fun processInstructions(instructions: List<Instruction>): List<Map<Int, Set<Pair<Int,Int>>>>{
    val target = 0
    val processingSteps = instructions.windowed(18, 18)
    val validResults = ArrayList<Map<Int, Set<Pair<Int,Int>>>>(processingSteps.size)
    val alu = ALU(emptyList<Int>().iterator())
    processingSteps.reversed().forEachIndexed { index, step ->
        println("Processing step: ${processingSteps.size - index}")
        val stepResults = mutableMapOf<Int, MutableSet<Pair<Int,Int>>>()
        (1..9).forEach { w ->
            (0..250_000).forEach { z ->
                alu.loadState(w, 0, 0, z)
                alu.executeInstructions(step.subList(1, step.size))
                val state = alu.dumpState()
                val result = state.last().value
                if (index == 0) {
                    if (result == target) {
                        stepResults[z] = stepResults.getOrDefault(z, mutableSetOf()).apply { add(Pair(w,result)) }
                    }
                } else {
                    if (result in validResults[index - 1]) {
                        stepResults[z] = stepResults.getOrDefault(z, mutableSetOf()).apply { add(Pair(w,result)) }
                    }
                }
            }
        }
        validResults.add(stepResults)
    }
    return validResults
}

private fun sol1(results: List<Map<Int, Set<Pair<Int, Int>>>>): String {
    val output = results.reversed().scan(Pair(0,0)) { acc, map ->
        map[acc.second]!!.maxByOrNull { it.first }!!
    }.map { it.first }.takeLast(results.size)
    return output.joinToString("")
}

private fun sol2(results: List<Map<Int, Set<Pair<Int, Int>>>>): String {
    val output = results.reversed().scan(Pair(0,0)) { acc, map ->
        map[acc.second]!!.minByOrNull { it.first }!!
    }.map { it.first }.takeLast(results.size)
    return output.joinToString("")
}

enum class InstructionType {
    INP,
    ADD,
    MUL,
    DIV,
    MOD,
    EQL,
}

data class Instruction(val type: InstructionType, val arg1: String, val arg2: String?) {
    override fun toString() = "$type $arg1 $arg2"
}


class ALU(private val input: Iterator<Int>) {
    data class Variable(val label: Char, var value: Int)
    private val variables = listOf(
        Variable('w', 0),
        Variable('x', 0),
        Variable('y', 0),
        Variable('z', 0),
    )

    fun loadState(w: Int, x: Int, y: Int, z: Int) {
        getVar('w').value = w
        getVar('x').value = x
        getVar('y').value = y
        getVar('z').value = z
    }

    fun dumpState() = variables.map(Variable::copy)

    private fun getVar(char: Char) = variables.first { it.label == char }

    private fun String.isVar() = "^[a-z]$".toRegex().matches(this)

    fun executeInstructions(instructions: List<Instruction>) {
        instructions.forEach { instruction ->
            val (type, arg1, arg2) = instruction
            when (type) {
                InstructionType.INP -> processInp(arg1)
                InstructionType.ADD -> processArithmetic(arg1, arg2!!) { a, b -> a + b }
                InstructionType.MUL -> processArithmetic(arg1, arg2!!) { a, b -> a * b }
                InstructionType.DIV -> processArithmetic(arg1, arg2!!) { a, b -> a / b }
                InstructionType.MOD -> processArithmetic(arg1, arg2!!) { a, b -> a % b }
                InstructionType.EQL -> processArithmetic(arg1, arg2!!) { a, b -> if (a == b) 1 else 0 }
            }
        }
    }

    private fun processInp(argument: String) {
        val writeTo = getVar(argument.first())
        val nextInput = input.next()
        writeTo.value = nextInput
    }

    private fun processArithmetic(arg1: String, arg2:String, operation: (Int, Int) -> Int) {
        val var1 = getVar(arg1.first())
        val value = if (arg2.isVar()) getVar(arg2.first()).value else arg2.toInt()
        var1.value = operation(var1.value, value)
    }
}