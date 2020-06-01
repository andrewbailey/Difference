package dev.andrewbailey.diff.impl

internal inline class CircularIntArray(
    val array: IntArray
) {

    constructor(size: Int) : this(IntArray(size))

    operator fun get(index: Int): Int {
        return array[toInternalIndex(index)]
    }

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
