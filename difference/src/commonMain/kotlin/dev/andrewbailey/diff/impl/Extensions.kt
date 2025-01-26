package dev.andrewbailey.diff.impl

import kotlin.math.abs

internal inline fun Int.isEven() = abs(this) % 2 == 0

internal inline fun Int.isOdd() = abs(this) % 2 == 1

internal inline fun <T> MutableList<T>.push(item: T) {
    add(item)
}

internal inline fun <T> MutableList<T>.pop(): T = removeAt(size - 1)

internal inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
    @Suppress("ReplaceManualRangeWithIndicesCalls")
    for (i in 0 until size) {
        action(this[i])
    }
}

internal inline fun <T> List<T>.fastForEachIndexed(action: (Int, T) -> Unit) {
    @Suppress("ReplaceManualRangeWithIndicesCalls")
    for (i in 0 until size) {
        action(i, this[i])
    }
}
