package dev.andrewbailey.diff.impl

internal sealed class MyersDiffOperation<out T> {

    data class Insert<T>(val value: T) : MyersDiffOperation<T>()

    data object Delete : MyersDiffOperation<Nothing>()

    data object Skip : MyersDiffOperation<Nothing>()
}
