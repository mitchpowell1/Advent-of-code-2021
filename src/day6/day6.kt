package day6

import utils.FileUtil
import java.util.Collections

fun main() {
    val rawInput = FileUtil.readFileToString("src/day6/input.txt")
        .split(',')
        .map(String::toInt)
    println(calculatePopulation(rawInput, 80))
    println(calculatePopulation(rawInput, 256))
}

fun calculatePopulation(fishList: List<Int>, days: Int) : Long {
    var fishCounts = mutableMapOf<Int, Long>()
    fishList.forEach{ fishCounts[it] = Collections.frequency(fishList, it).toLong() }

    repeat(days) {
        val nextFish: MutableMap<Int, Long> = mutableMapOf()
        fishCounts.forEach { (k, v) ->
            if (k == 0) {
                nextFish[8] = v
                nextFish[6] = nextFish.getOrDefault(6, 0L) + v
            } else {
                nextFish[k - 1] = nextFish.getOrDefault(k -1, 0L) + v
            }
        }
        fishCounts = nextFish
    }
    return fishCounts.values.sum()
}