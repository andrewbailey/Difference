package dev.andrewbailey.diff.impl

import kotlin.math.abs

internal fun Int.isEven() = abs(this) % 2 == 0

internal fun Int.isOdd() = abs(this) % 2 == 1

internal fun <T> MutableList<T>.push(item: T) {
    add(item)
}

internal fun <T> MutableList<T>.pop(): T {
    check(isNotEmpty()) {
        "List has no items"
    }
    return removeAt(size - 1)
}
