package dev.andrewbailey.diff.impl

import kotlin.jvm.JvmInline

@JvmInline
internal value class CircularIntArray(private val array: IntArray) {

    constructor(size: Int) : this(IntArray(size))

    operator fun get(index: Int): Int = array[toInternalIndex(index)]

    operator fun set(index: Int, value: Int) {
        array[toInternalIndex(index)] = value
    }

    private fun toInternalIndex(index: Int): Int {
        val moddedIndex = index % array.size
        return if (moddedIndex < 0) {
            moddedIndex + array.size
        } else {
            moddedIndex
        }
    }
}
