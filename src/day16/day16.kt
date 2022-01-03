package day16

import utils.FileUtil

fun main() {
    val input = FileUtil.readFileToString("./src/day16/input.txt")
        .map { it.toString().toInt(16).toString(2).padStart(4,'0').toList() }
        .flatten()
    val reader = PacketReader(input)
    val packet = reader.readPacket()
    println(packet.getVersionSum())
    println(packet.compute())
}


enum class PacketType(val code: Int) {
    SUM(0),
    PRODUCT(1),
    MINIMUM(2),
    MAXIMUM(3),
    NUMBER_LITERAL(4),
    GREATER_THAN(5),
    LESS_THAN(6),
    EQUAL_TO(7);

    companion object{
        fun fromInt(input: Int) : PacketType? {
            return values().firstOrNull { it.code == input }
        }
    }
}

abstract class Packet(private val version: Int, private val type: PacketType, var subPackets: List<Packet>? = null) {
    fun getVersionSum(): Int {
        return version + (subPackets?.sumOf { it.getVersionSum() } ?: 0)
    }

    fun compute(): Long = when (type) {
        PacketType.SUM -> subPackets?.sumOf { it.compute() } ?: 0L
        PacketType.PRODUCT -> subPackets?.fold(1L) { acc, v -> acc * v.compute() } ?: 0L
        PacketType.MINIMUM -> subPackets?.minOf { it.compute() } ?: 0L
        PacketType.MAXIMUM -> subPackets?.maxOf { it.compute() } ?: 0L
        PacketType.NUMBER_LITERAL -> (this as? NumberLiteralPacket)?.value ?: 0L
        PacketType.GREATER_THAN -> if (subPackets!![0].compute() > subPackets!![1].compute()) 1 else 0
        PacketType.LESS_THAN -> if (subPackets!![0].compute() < subPackets!![1].compute()) 1 else 0
        PacketType.EQUAL_TO -> if (subPackets!![0].compute() == subPackets!![1].compute()) 1 else 0
    }
}

class NumberLiteralPacket(version: Int, val value: Long) : Packet(version, PacketType.NUMBER_LITERAL)
class OperatorPacket(version: Int, subPackets: List<Packet>, type: PacketType) : Packet(version, type, subPackets)

class PacketReader(private val bits: List<Char>) {
    private enum class LengthEncoding(val dataSize: Int) {
        NUM_PACKETS(11),
        NUM_BITS(15);
        companion object {
            fun fromBit(bit: Char) = if (bit == '1') NUM_PACKETS else NUM_BITS
        }
    }

    private var readerIndex = 0

    private fun readOperatorPacket(version: Int, operatorType: PacketType): OperatorPacket {
        val lengthEncoding = LengthEncoding.fromBit(readForward(1).first())

        val subPackets: MutableList<Packet> = mutableListOf()
        when (lengthEncoding) {
            LengthEncoding.NUM_PACKETS -> {
                val numberOfSubPackets = readForward(lengthEncoding.dataSize).toInt()
                repeat(numberOfSubPackets) {
                    subPackets.add(readPacket())
                }
            }
            LengthEncoding.NUM_BITS -> {
                val numberOfBits = readForward(lengthEncoding.dataSize).toInt()
                val target = readerIndex + numberOfBits
                while (readerIndex < target) {
                    subPackets.add(readPacket())
                }
            }
        }

        return OperatorPacket(version, subPackets, operatorType)
    }

    private fun readNumberLiteralPacket(version: Int): NumberLiteralPacket {
        val groups: MutableList<List<Char>> = mutableListOf()
        var group: List<Char>? = null
        while(group?.first() != '0') {
            group = readForward(5)
            groups.add(group.slice(1 until 5))
        }
        val value = groups.flatten().toLong()
        return NumberLiteralPacket(version, value)
    }

    private fun readForward(numBits: Int): List<Char> {
        val output = bits.slice(readerIndex until readerIndex + numBits)
        readerIndex += numBits
        return output
    }

    fun readPacket(): Packet {
        val (version, type) = readForward(6)
            .windowed(3,3)
            .map { it.toInt() }

        var parsedType = PacketType.fromInt(type)!!
        return when(parsedType) {
            PacketType.NUMBER_LITERAL -> readNumberLiteralPacket(version)
            else -> readOperatorPacket(version, parsedType)
        }
    }

    private fun List<Char>.toInt() = this.joinToString("").toInt(2)
    private fun List<Char>.toLong() = this.joinToString("").toLong(2)
}