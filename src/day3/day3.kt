package day3

import utils.FileUtil

fun getMostCommonBitAtPosition(input: List<String>, position: Int) : Int {
    val oneCount = input.count { it[position] == '1' }
    return if (oneCount >= input.size / 2.0) 1 else 0
}

enum class BitCondition {
    O2 {
        override fun applyTo(bit: Int) = bit
    },
    CO2 {
        override fun applyTo(bit: Int) = bit xor 1
    };

    abstract fun applyTo(targetBit: Int): Int
}

fun filterByBitCondition(inputs: List<String>, bitCondition: BitCondition) : Int {
    var inputCopy: List<String> = ArrayList(inputs)
    var i = 0
    while (inputCopy.size > 1) {
        val mostCommonBit = getMostCommonBitAtPosition(inputCopy, i)
        val targetBit = bitCondition.applyTo(mostCommonBit)
        inputCopy = inputCopy.filter {
            Character.getNumericValue(it[i]) == targetBit
        }
        i += 1
    }

    return Integer.parseInt(inputCopy.last(), 2)
}

fun sol1(inputs: List<String>) : Int {
    var gammaRate = 0
    var epsilonRate = 0
    val wordSize = inputs[0].length

    for(i in 0 until wordSize) {
        val mostCommon = getMostCommonBitAtPosition(inputs, i)
        gammaRate = (gammaRate shl 1) or mostCommon
        epsilonRate = (epsilonRate shl 1) or (mostCommon xor 1)
    }
    return gammaRate * epsilonRate
}

fun sol2(inputs: List<String>) = BitCondition.values()
        .map{ filterByBitCondition(inputs, it) }
        .reduce(Int::times)

fun main() {
    val input = FileUtil.readFileAsStrings("src/day3/input.txt")
    println(sol1(input))
    println(sol2(input))
}