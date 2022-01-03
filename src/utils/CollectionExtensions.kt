package utils

import java.util.*

//typealias Grid<T> =  List<List<T>>

/**
 * Collection extensions namespace
 */
object CollectionExtensions {
    /**
     * Returns a sequence of the elements of the cartesian product of this collection with the other collection
     *
     * Example Usage:
     * val a = listOf('a', 'b')
     * val b = listOf(1, 2)
     *
     * a.product(b) // ('a', 1), ('a', 2), ('b', 1), ('b', 2)
     */
    fun <T,U> Iterable<T>.product(other: Iterable<U>) = sequence {
        this@product.forEach { a ->
            other.forEach { b ->
                yield(Pair(a, b))
            }
        }
    }

//    fun IntRange.product(other: IntRange) = sequence {
//        this@product.forEach { a ->
//            other.forEach { b ->
//                yield(Pair(a, b))
//            }
//        }
//
//    }

    fun <T> List<T>.countByLong(): Map<T, Long> {
        var counts = mutableMapOf<T, Long>()
        this.forEach{ counts[it] = Collections.frequency(this, it).toLong() }
        return counts
    }

    fun <T> List<MutableList<T>>.toGrid(): Grid<T> = Grid(this)
}
