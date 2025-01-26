package dev.andrewbailey.diff.impl

import kotlin.jvm.JvmInline

@JvmInline
internal value class CircularIntArray(val array: IntArray) {

    constructor(size: Int) : this(IntArray(size))

    inline operator fun get(index: Int): Int = array[toLinearIndex(index)]

    inline operator fun set(index: Int, value: Int) {
        array[toLinearIndex(index)] = value
    }

    private inline fun toLinearIndex(index: Int): Int {
        val moddedIndex = index % array.size
        return if (moddedIndex < 0) {
            moddedIndex + array.size
        } else {
            moddedIndex
        }
    }
}
