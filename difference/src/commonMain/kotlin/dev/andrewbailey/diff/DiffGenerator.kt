package dev.andrewbailey.diff

import dev.andrewbailey.diff.DiffOperation.Add
import dev.andrewbailey.diff.DiffOperation.AddAll
import dev.andrewbailey.diff.DiffOperation.Move
import dev.andrewbailey.diff.DiffOperation.MoveRange
import dev.andrewbailey.diff.DiffOperation.Remove
import dev.andrewbailey.diff.DiffOperation.RemoveRange
import dev.andrewbailey.diff.impl.MyersDiffAlgorithm
import dev.andrewbailey.diff.impl.MyersDiffOperation.Delete
import dev.andrewbailey.diff.impl.MyersDiffOperation.Insert
import dev.andrewbailey.diff.impl.MyersDiffOperation.Skip
import dev.andrewbailey.diff.impl.fastForEach

internal object DiffGenerator {

    fun <T> generateDiff(
        original: List<T>,
        updated: List<T>,
        detectMoves: Boolean
    ): DiffResult<T> {
        val diff = MyersDiffAlgorithm(original, updated).generateDiff()

        var index = 0
        var indexInOriginalSequence = 0
        val operations = mutableListOf<DiffOperation<T>>()
        diff.fastForEach { operation ->
            when (operation) {
                is Insert -> {
                    operations += Add(
                        index = index,
                        item = operation.value
                    )
                    index++
                }
                is Delete -> {
                    operations += Remove(
                        index = index,
                        item = original[indexInOriginalSequence]
                    )
                    indexInOriginalSequence++
                }
                is Skip -> {
                    index++
                    indexInOriginalSequence++
                }
            }
        }

        if (detectMoves) reduceDeletesAndAddsToMoves(operations)
        reduceSequences(operations)
        return DiffResult(operations)
    }

    /**
     * Given a diff, this function performs a reduction step that converts pairs of adds and removes
     * for the same item into move operations.
     *
     * This function takes O(n^2) time. There's an optimization in place here that avoids rescanning
     * the entire array for possible matches, and technically adds a coefficient of 1/2 to this
     * runtime, but alas big-O isn't concerned with this detail.
     *
     * The way this algorithm runs is a bit unintuitive and depends on the inputted list of
     * operations being sorted by index from smallest to largest. When this function is called, the
     * [operations] only contains add and remove operations. We iterate over the entire array. For
     * each operation (whether it's an add or remove) we try and find an operation that undoes the
     * operation later on in the array (we don't need to rescan the earlier part for reasons that
     * will become clear in a moment).
     *
     * If we find a pair of operations that can be reduced to a move (i.e. if we find a remove that
     * undoes an add or vice versa), we replace the operation that came first with the move
     * operation. This keeps the array in a sorted order when we're done. We then remove the
     * opposite operation from the diff and check the next item until we've checked all operations.
     */
    private fun <T> reduceDeletesAndAddsToMoves(operations: MutableList<DiffOperation<T>>) {
        var index = 0
        while (index < operations.size) {
            val operation = operations[index]

            var indexOfOppositeAction = index + 1
            var endIndexDifference = 0

            while (indexOfOppositeAction < operations.size &&
                !canBeReducedToMove(operation, operations[indexOfOppositeAction])
            ) {
                val rejectedOperation = operations[indexOfOppositeAction]
                if (rejectedOperation is Add<T>) {
                    endIndexDifference++
                } else {
                    endIndexDifference--
                }
                indexOfOppositeAction++
            }

            val oppositeAction = operations.getOrNull(indexOfOppositeAction)

            if (oppositeAction != null) {
                val deleteFromIndex = if (operation is Remove<T>) {
                    operation.index
                } else {
                    (oppositeAction as Remove<T>).index - endIndexDifference - 1
                }

                val addToIndex = if (operation is Add<T>) {
                    operation.index
                } else {
                    (oppositeAction as Add<T>).index - endIndexDifference + 1
                }

                operations[index] = Move(
                    fromIndex = deleteFromIndex,
                    toIndex = addToIndex
                )

                operations.removeAt(indexOfOppositeAction)
            }

            index++
        }
    }

    private fun <T> canBeReducedToMove(
        operation1: DiffOperation<T>,
        operation2: DiffOperation<T>
    ): Boolean = when (operation1) {
        is Add<T> -> operation2 is Remove<T> && operation1.item == operation2.item
        is Remove<T> -> operation2 is Add<T> && operation1.item == operation2.item
        else -> false
    }

    private fun <T> reduceSequences(operations: MutableList<DiffOperation<T>>) {
        var index = 0

        while (index < operations.size) {
            val operationToReduce = operations[index]
            var sequenceEndIndex = index + 1
            var sequenceLength = 1
            while (sequenceEndIndex < operations.size &&
                operationToReduce.canBeCombinedWith(operations[sequenceEndIndex], sequenceLength)
            ) {
                sequenceEndIndex++
                sequenceLength++
            }

            if (sequenceLength > 1) {
                operations[index] = reduceSequence(
                    operations = operations,
                    sequenceStartIndex = index,
                    sequenceLength = sequenceLength
                )

                repeat(sequenceLength - 1) { operations.removeAt(index + 1) }
            }

            index++
        }
    }

    private fun <T> reduceSequence(
        operations: MutableList<DiffOperation<T>>,
        sequenceStartIndex: Int,
        sequenceLength: Int
    ): DiffOperation<T> = when (val startOperation = operations[sequenceStartIndex]) {
        is Remove -> {
            RemoveRange(
                startIndex = startOperation.index,
                endIndex = startOperation.index + sequenceLength
            )
        }
        is Add -> {
            AddAll(
                index = startOperation.index,
                items = List(sequenceLength) { i ->
                    (operations[sequenceStartIndex + i] as Add<T>).item
                }
            )
        }
        is Move -> {
            MoveRange(
                fromIndex = startOperation.fromIndex,
                toIndex = startOperation.toIndex,
                itemCount = sequenceLength
            )
        }
        else -> throw IllegalArgumentException(
            "Cannot reduce sequence starting with $startOperation"
        )
    }

    private fun <T> DiffOperation<T>.canBeCombinedWith(
        otherOperation: DiffOperation<T>,
        offset: Int
    ): Boolean = when (this) {
        is Remove -> otherOperation is Remove && index == otherOperation.index
        is Add -> otherOperation is Add && index + offset == otherOperation.index
        is Move ->
            otherOperation is Move &&
                when {
                    toIndex < fromIndex -> {
                        // Move backwards case
                        toIndex + offset == otherOperation.toIndex &&
                            fromIndex + offset == otherOperation.fromIndex
                    }
                    else -> {
                        // Move forwards case
                        toIndex == otherOperation.toIndex &&
                            fromIndex == otherOperation.fromIndex
                    }
                }
        else -> false
    }
}
