package day20

import utils.CollectionExtensions.product
import utils.FileUtil

fun main() {
    val (algorithm, rawSeedImage) = FileUtil.readFileToString("./src/day20/input.txt")
        .split("""\n{2,}""".toRegex())
    val seedImage = rawSeedImage.lines().map { it.map { c -> c } }
    sol1(seedImage, algorithm)
    sol2(seedImage, algorithm)
}

fun sol1(seedImage: List<List<Char>>, algorithm: String) {
    val enhanced = applyEnhancements(seedImage, algorithm, 2)
    println(enhanced.sumOf { r -> r.count { it == '#' }})
}

fun sol2(seedImage: List<List<Char>>, algorithm: String) {
    val enhanced = applyEnhancements(seedImage, algorithm, 50)
    println(enhanced.sumOf { r -> r.count { it == '#' }})
}
fun applyEnhancements(seedImage: List<List<Char>>, algorithm: String, times: Int): List<List<Char>> {
    var image = seedImage
    repeat(times) {
        val backgroundColor = if (algorithm[0] == '.') '.' else if (it % 2 == 0) '.' else '#'
        image = enhanceImage(image, algorithm, backgroundColor)
    }
    return image
}
fun enhanceImage(image: List<List<Char>>, algorithm: String, backgroundColor: Char): List<List<Char>> {
    val newImage = Array(image.size + 2) { Array(image[0].size + 2) { backgroundColor }}
    image.indices.forEach { row ->
        image[0].indices.forEach { col ->
            newImage[row + 1][col + 1] = image[row][col]
        }
    }
    val newImageClone = newImage.map{it.clone()}
    newImage.indices.forEach { row ->
        newImage[0].indices.forEach { col ->
            newImageClone[row][col] = computeNextPixel(row, col, newImage, algorithm, backgroundColor)
        }
    }

    return newImageClone.map { it.toList() }.toList()
}

fun computeNextPixel(row: Int, col: Int, image: Array<Array<Char>>, algorithm: String, backgroundColor: Char): Char {
    val neighborCoordinates = getNeighborCoords(row, col)
    val newPixelCode = neighborCoordinates
        .map { getOrDefault(it, image, backgroundColor) }
        .map {
            when(it) {
                '.' -> '0'
                '#' -> '1'
                else -> error("Bad input")
            }
        }
        .joinToString("")
    return algorithm[newPixelCode.toInt(2)]
}

fun getOrDefault(coords: Pair<Int, Int>, image: Array<Array<Char>>, default: Char): Char {
    val (x, y) = coords
    if (x < 0 || x >= image.size || y < 0 || y >= image[0].size) {
        return default
    }
    return image[x][y]
}

fun getNeighborCoords(row: Int, col: Int) = (row - 1 .. row + 1).product(col - 1 .. col + 1).toList()
