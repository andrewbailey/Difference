package dev.andrewbailey.diff.impl

internal data class Snake(
    val start: Point,
    val end: Point
) : Comparable<Snake> {
    override fun compareTo(other: Snake): Int = if (start.x == other.start.x) {
        start.y - other.start.y
    } else {
        start.x - other.start.x
    }
}
