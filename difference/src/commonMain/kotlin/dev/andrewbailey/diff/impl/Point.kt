package dev.andrewbailey.diff.impl

import kotlin.jvm.JvmInline

@JvmInline
internal value class Point(private val packed: Long) {
    val x: Int get() = (packed and 0xFFFFFFFF).toInt()
    val y: Int get() = (packed shr 32).toInt()

    constructor(x: Int, y: Int) : this(
        (x.toLong() and 0xFFFFFFFF) or (y.toLong() shl 32)
    )

    inline operator fun component1() = x
    inline operator fun component2() = y
}
