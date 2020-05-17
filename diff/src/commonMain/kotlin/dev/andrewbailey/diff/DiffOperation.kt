package dev.andrewbailey.diff

sealed class DiffOperation<T> {

    data class Remove<T>(
        val index: Int
    ) : DiffOperation<T>()

    data class RemoveRange<T>(
        val startIndex: Int,
        val endIndex: Int
    ) : DiffOperation<T>()

    data class Add<T>(
        val index: Int,
        val item: T
    ) : DiffOperation<T>()

    data class AddAll<T>(
        val index: Int,
        val items: List<T>
    ) : DiffOperation<T>()

    data class Move<T>(
        val fromIndex: Int,
        val toIndex: Int
    ) : DiffOperation<T>()

    data class MoveRange<T>(
        val fromIndex: Int,
        val toIndex: Int,
        val itemCount: Int
    ) : DiffOperation<T>()

}
