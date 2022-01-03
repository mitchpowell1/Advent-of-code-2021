package day18

import utils.CollectionExtensions.product
import utils.FileUtil
import java.util.*
import kotlin.math.ceil

fun main() {
    val input = FileUtil.readLinesWithTransform("./src/day18/input.txt", ::parseSFNumber)
    val output = input.reduce(::addSFNumbers)
    println(output.magnitude)
    println(findMax2(input))
}

fun findMax2(input: List<SFNumber>): Int {
    var max = Int.MIN_VALUE
    input.product(input).forEach { (n1, n2) ->
        if (n1 != n2) {
            val result = addSFNumbers(n1, n2).magnitude
            if (result > max) {
                max = result
            }
        }
    }
    return max
}

fun parseSFNumber(line: String): SFNumber {
    var sfNumStack: Stack<SFNumber> = Stack()
    sfNumStack.push(SFNumber())
    var readerIndex = 1
    while (sfNumStack.isNotEmpty()) {
        while (readerIndex < line.length) {
            when(line[readerIndex]) {
                '[' -> {
                    sfNumStack.push(SFNumber())
                    readerIndex += 1
                }
                ',' -> {
                    readerIndex += 1
                    continue
                }
                ']' -> {
                    val lastPopped = sfNumStack.pop()
                    if (sfNumStack.isNotEmpty()) {
                        sfNumStack.peek().addChild(lastPopped)
                        lastPopped.parent = sfNumStack.peek()
                        readerIndex += 1
                    } else {
                        return lastPopped
                    }
                }
                else -> {
                    val (nextVal,) = """^(\d+)""".toRegex().find(line.substring(readerIndex))!!.destructured
                    val newSFNumber = SFNumber().apply { value = nextVal.toInt() }
                    sfNumStack.peek().addChild(newSFNumber)
                    newSFNumber.parent = sfNumStack.peek()
                    readerIndex += nextVal.length
                    continue
                }
            }
        }
    }
    throw error("You shouldn't be able to get this far")
}

fun addSFNumbers(num1: SFNumber, num2: SFNumber): SFNumber {
    val n1 = num1.clone()
    val n2 = num2.clone()
    val newSFNumber = SFNumber().apply {
        left = n1
        right = n2
    }
    n1.parent = newSFNumber
    n2.parent = newSFNumber

    newSFNumber.reduce()
    return newSFNumber
}

class SFNumber {
    var left: SFNumber? = null
    var right: SFNumber? = null
    var parent: SFNumber? = null
    var value: Int? = null
    val depth: Int
        get() {
            var _depth = 0
            var node: SFNumber = this
            while (node.parent != null) {
                _depth += 1
                node = node.parent!!
            }
            return _depth
        }

    val magnitude: Int
        get() {
            return if (value != null) value!! else (3 * left!!.magnitude) + (2 * right!!.magnitude)
        }

    private val canExplode: Boolean
        get() {
            return depth >= 4 && left != null && right != null && left?.value != null && right?.value != null
        }

    private val canSplit: Boolean
        get(){
            return value != null && value!! >= 10
        }

    fun addChild(value: SFNumber) {
        if (left == null) {
            left = value
        } else if (right == null) {
            right = value
        } else {
            throw error("You did something incorrect to get here")
        }
    }

    fun clone(parentNode: SFNumber? = null): SFNumber {
        val thisVal = value
        val thisLeft = left
        val thisRight = right
        val newNum = SFNumber().apply {
            parent = parentNode
            value = thisVal
            left = thisLeft?.clone(this)
            right = thisRight?.clone(this)
        }
        return newNum
    }

    fun reduce() {
        while(findFirst { it.canExplode } != null || findFirst { it.canSplit } != null) {
            val nextToExplode = findFirst { it.canExplode }
            if (nextToExplode != null) {
                nextToExplode.explode()
                continue
            }
            val nextToSplit = findFirst { it.canSplit }
            nextToSplit?.split()
        }
    }

    fun split() {
        val thisValue = value
        left = SFNumber().apply {
            value = thisValue!! / 2
            parent = this@SFNumber
        }
        right = SFNumber().apply {
            value = ceil(thisValue!!.toDouble() / 2).toInt()
            parent = this@SFNumber
        }
        value = null
    }

    private fun getFullFrontier(): List<SFNumber> {
        var node: SFNumber? = this
        while (node!!.parent != null) {
            node = node.parent!!
        }
        val frontier: MutableList<SFNumber> = mutableListOf()
        fun helper(node: SFNumber) {
            if (node.value != null) {
                frontier.add(node)
                return
            }
            if (node.left != null) {
                helper(node.left!!)
            }
            if (node.right != null) {
                helper(node.right!!)
            }
        }
        helper(node)
        return frontier
    }

    private fun findFirst(condition: (SFNumber) -> Boolean): SFNumber? {
        if (condition(this)) {
            return this
        }
        val leftResult = left?.findFirst(condition)
        if (leftResult != null)  { return leftResult }
        return right?.findFirst(condition)
    }

    private fun explode() {
        if (!canExplode) return
        val frontier = getFullFrontier()
        // Propagate right
        val leftFrontierIndex = frontier.indexOf(left)
        if (leftFrontierIndex > 0) {
            val nextLeftLeaf = frontier[leftFrontierIndex - 1]
            nextLeftLeaf.value = nextLeftLeaf.value!! + left!!.value!!
        }

        // Propagate left
        val rightFrontierIndex = frontier.indexOf(right)
        if (rightFrontierIndex < frontier.size - 1) {
            val nextRightLeaf = frontier[rightFrontierIndex + 1]
            nextRightLeaf.value = nextRightLeaf.value!! + right!!.value!!
        }
        left = null
        right = null
        value = 0
    }

    override fun toString() = if (value != null) value.toString() else "[${left.toString()},${right.toString()}]"
}