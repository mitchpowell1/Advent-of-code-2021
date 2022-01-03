package day23

import utils.CollectionExtensions.toGrid
import utils.FileUtil
import java.util.*
import kotlin.system.measureTimeMillis
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@OptIn(ExperimentalTime::class)
fun main() {
    val input1 = FileUtil.readFileToString("./src/day23/input.txt")
    val input2 = FileUtil.readFileToString("./src/day23/input2.txt")
    val burrow1 = constructBurrow(input1)
    val burrow2 = constructBurrow(input2)
    val t1 = measureTimeMillis { println(sol1(burrow1)) }
    val t2 = measureTimeMillis { println(sol1(burrow2)) }

    println("P1 Duration: ${Duration.milliseconds(t1)}")
    println("P2 Duration: ${Duration.milliseconds(t2)}")
}

fun sol1(burrow: AmphipodBurrow): Int {
    val toEvaluate: PriorityQueue<Pair<AmphipodBurrow, Int>> = PriorityQueue() { a,b ->
        a.second.compareTo(b.second)
    }

    var minimumCost = Int.MAX_VALUE

    val encounteredBurrows: MutableSet<String> = mutableSetOf()
    toEvaluate.add(Pair(burrow, 0))
    while (toEvaluate.isNotEmpty()) {
        val (nextBurrow, cost) = toEvaluate.poll()
        if (nextBurrow.getId() in encounteredBurrows) { continue }
        encounteredBurrows += nextBurrow.getId()
        if (nextBurrow.isSolved() && cost < minimumCost) {
            minimumCost = cost
            return minimumCost
        }
        val allAvailableMoves = nextBurrow.amphipods
            .flatMap { it.getAvailableMoves().map { m -> Pair(nextBurrow.applyMove(m), m) }}
            .filter { it.first.getId() !in encounteredBurrows && cost + it.second.cost < minimumCost }

        allAvailableMoves.forEach {
            toEvaluate.add(Pair(it.first,  cost + it.second.cost))
        }
    }

    return -1
}

abstract class BurrowNode(private val label: String) {
    open val moveInFactor: Int = 1
    open val moveOutFactor: Int = 0

    abstract fun clone(): BurrowNode

    override fun toString(): String {
        return label
    }

    open fun getId() = if (isOccupied) occupant!!.type.char.toString() else "."

    open val occupant: Amphipod?
    get() {
        return _occupant
    }

    private var _occupant: Amphipod? = null

    open val isOccupied: Boolean
    get() { return _occupant != null }

    val neighbors: List<BurrowNode>
    get() {
        return _neighbors
    }

    private val _neighbors: MutableList<BurrowNode> = mutableListOf()

    fun addNeighbor(node: BurrowNode) {
        _neighbors.add(node)
    }

    open fun addOccupant(occupant: Amphipod) {
        _occupant = occupant
        occupant.currentRoom = this
    }

    open fun isAvailable(): Boolean {
        return !isOccupied && !isAntechamber()
    }

    open fun vacate() {
        _occupant = null
    }

    fun isAntechamber() = neighbors.any { it is StackRoom }
}

class StackRoom(val type: AmphipodType, private val label: String, private val capacity: Int): BurrowNode(label) {
    override fun clone(): BurrowNode = StackRoom(type, label, capacity)
    private val stack: Stack<Amphipod> = Stack()

    fun getOccupants(): List<Amphipod> {
        return (0 until stack.size).map { stack[it] }
    }

    override fun getId() = getOccupants()
        .map { it.type.char }
        .joinToString("")
        .padStart(capacity, '.')

    override val moveInFactor: Int
        get() = capacity - stack.size

    override val moveOutFactor: Int
        get() = capacity - stack.size

    override fun isAvailable(): Boolean {
        return stack.size < capacity && stack.none { it.type != type }
    }

    override val isOccupied: Boolean
        get() = stack.size == capacity

    override val occupant: Amphipod?
        get() = if (stack.isEmpty()) null else stack.peek()

    override fun addOccupant(occupant: Amphipod) {
        stack.push(occupant)
        occupant.currentRoom = this
    }

    override fun vacate() {
        stack.pop()
    }
}

class BurrowHallway(private val label: String): BurrowNode("$label:Hallway") {
    override fun clone() = BurrowHallway(label)
}

class Amphipod(val type: AmphipodType) {
    fun copy(): Amphipod {
        return Amphipod(type)
    }
    private val movementWeight = type.movementWeight

    companion object {
        fun fromChar(char: Char) = when (char) {
            'A' -> Amphipod(AmphipodType.AMBER)
            'B' -> Amphipod(AmphipodType.BRONZE)
            'C' -> Amphipod(AmphipodType.COPPER)
            'D' -> Amphipod(AmphipodType.DESERT)
            else -> error("Invalid Amphipod Type: $char")
        }
    }

    lateinit var currentRoom: BurrowNode

    fun getAvailableMoves(): Set<Move> {
        if (currentRoom is StackRoom && currentRoom.occupant != this) return emptySet()
        if (currentRoom is StackRoom && (currentRoom as StackRoom).type == type && (currentRoom as StackRoom).getOccupants().all { it.type == type}) return emptySet()
        val evaluated: MutableSet<BurrowNode> = mutableSetOf()
        val toVisit: Queue<Pair<BurrowNode, Int>> = ArrayDeque()
        val potentialMoves: MutableSet<Move> = mutableSetOf()
        toVisit.add(Pair(currentRoom, 0))
        while (toVisit.isNotEmpty()) {
            val (node, cost) = toVisit.poll()
            node.neighbors
                .filter { !it.isOccupied && it !in evaluated }
                .forEach {
                    toVisit.add(
                        Pair(
                            it,
                            cost + (node.moveOutFactor * movementWeight) + (it.moveInFactor * movementWeight)
                        )
                    )
                }
            evaluated.add(node)
            val nodeIsMatchedRoom = node is StackRoom && node.type == type
            val canMove = (currentRoom is BurrowHallway && nodeIsMatchedRoom) || (currentRoom is StackRoom && (node is BurrowHallway || nodeIsMatchedRoom))
            if (node != currentRoom && !node.isAntechamber() && node.isAvailable() && canMove) {
                potentialMoves.add(Move(currentRoom, node, cost))
            }
        }
        return potentialMoves
    }
}

class AmphipodBurrow(val amphipods: List<Amphipod>, private val nodes: List<BurrowNode>) {
    fun isSolved() = amphipods.all { it.currentRoom is StackRoom && (it.currentRoom as StackRoom).type == it.type }

    fun getId() = nodes.joinToString("", transform = BurrowNode::getId)

    fun applyMove(move: Move): AmphipodBurrow {
        val newAmphipods = amphipods.map(Amphipod::copy)
        val newNodes = nodes.map(BurrowNode::clone)
        newNodes.forEachIndexed { index, node ->
            val oldNeighborIndices = nodes[index].neighbors.map{ nodes.indexOf(it) }
            oldNeighborIndices.forEach { node.addNeighbor(newNodes[it]) }
        }

        newAmphipods.forEachIndexed { index, amphipod ->
            val oldAmphipod = amphipods[index]
            if (oldAmphipod.currentRoom !is StackRoom) {
                val oldOccupancyIndex = nodes.indexOf(oldAmphipod.currentRoom)
                newNodes[oldOccupancyIndex].addOccupant(amphipod)
            }
        }

        newNodes.mapIndexed { index, room ->
            if (room is StackRoom) {
                val oldRoom = nodes[index]
                val oldOccupants = (oldRoom as StackRoom).getOccupants()
                oldOccupants.forEach {
                    val oldOccupantIndex = amphipods.indexOf(it)
                    room.addOccupant(newAmphipods[oldOccupantIndex])
                }
            }
        }

        val fromIndex = nodes.indexOf(move.from)
        val toIndex = nodes.indexOf(move.to)
        val occupant = newNodes[fromIndex].occupant
        newNodes[fromIndex].vacate()
        newNodes[toIndex].addOccupant(occupant!!)

        return AmphipodBurrow(newAmphipods, newNodes)
    }
}

enum class AmphipodType(val movementWeight: Int) {
    AMBER(1),
    BRONZE(10),
    COPPER(100),
    DESERT(1000);

    val char: Char
    get() {
        return when (this) {
            AMBER -> 'A'
            BRONZE -> 'B'
            COPPER -> 'C'
            DESERT -> 'D'
        }
    }
}

fun constructBurrow(input: String): AmphipodBurrow {
    val hallways = (0..10).map { BurrowHallway(it.toString()) }
    hallways.forEachIndexed { index, room ->
        hallways.getOrNull(index - 1)?.let { room.addNeighbor(it) }
        hallways.getOrNull(index + 1)?.let { room.addNeighbor(it) }
    }

    val pods = input.lines().subList(2, input.lines().size - 1)
        .map {
            it
                .filter { c -> "[A-Z]".toRegex().matches(c.toString()) }
                .map { c -> Amphipod.fromChar(c)}
                .toMutableList()
        }
        .toGrid()
    val rooms = pods.columns.mapIndexed { colIndex, c ->
        val roomType = AmphipodType.values()[colIndex]
        StackRoom(roomType, "Room:$roomType", c.size).apply {
            c.reversed().forEach {
                addOccupant(it)
            }
            addNeighbor(hallways[2 * colIndex + 2])
            hallways[2 * colIndex + 2].addNeighbor(this)
        }
    }

    return AmphipodBurrow(pods.flatten(), hallways + rooms)
}


data class Move(val from: BurrowNode, val to: BurrowNode, val cost: Int)