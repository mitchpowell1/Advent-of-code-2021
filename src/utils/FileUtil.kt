package utils

import java.io.File

object FileUtil {
    fun readFileAsInts(filename: String) = File(filename)
        .readLines()
        .map(String::toInt)

    fun readFileAsStrings(filename: String) = File(filename)
        .readLines()

    fun readFileToString(filename:String) = File(filename).readText().trim()

    fun <T> readLinesWithTransform(filename: String, transform: (v: String) -> T) = File(filename)
        .readLines()
        .map(transform)
}