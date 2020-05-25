package dev.andrewbailey.diff

import dev.andrewbailey.diff.DiffOperation.*
import dev.andrewbailey.diff.impl.MyersDiffAlgorithm
import dev.andrewbailey.diff.impl.MyersDiffOperation.*

object DiffGenerator {

    fun <T> generateDiff(
        original: List<T>,
        updated: List<T>
    ): DiffResult<T> {
        val diff = MyersDiffAlgorithm(original, updated)
            .generateDiff()

        var index = 0
        val operations = mutableListOf<DiffOperation<T>>()

        diff.forEach { operation ->
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
                        index = index
                    )
                }
                is Skip -> {
                    index++
                }
            }
        }

        return DiffResult(
            operations = reduceSequences(operations)
        )
    }

    private fun <T> reduceSequences(
        operations: MutableList<DiffOperation<T>>
    ): List<DiffOperation<T>> {
        val result = mutableListOf<DiffOperation<T>>()
        var index = 0

        while (index < operations.size) {
            val operationToReduce = operations[index]
            var sequenceEndIndex = index + 1
            var sequenceLength = 1
            while (sequenceEndIndex < operations.size &&
                operationToReduce.canBeCombinedWith(operations[sequenceEndIndex], sequenceLength)) {
                sequenceEndIndex++
                sequenceLength++
            }

            result += reduceSequence(
                operations = operations,
                sequenceStartIndex = index,
                sequenceEndIndex = sequenceEndIndex
            )

            index += sequenceLength
        }

        return result
    }

    private fun <T> reduceSequence(
        operations: MutableList<DiffOperation<T>>,
        sequenceStartIndex: Int,
        sequenceEndIndex: Int
    ): DiffOperation<T> {
        return if (sequenceStartIndex == sequenceEndIndex - 1) {
            operations[sequenceStartIndex]
        } else when (val startOperation = operations[sequenceStartIndex]) {
            is Remove -> {
                RemoveRange(
                    startIndex = startOperation.index,
                    endIndex = startOperation.index + (sequenceEndIndex - sequenceStartIndex)
                )
            }
            is Add -> {
                AddAll(
                    index = startOperation.index,
                    items = operations.subList(sequenceStartIndex, sequenceEndIndex)
                        .asSequence()
                        .map { operation ->
                            require(operation is Add<T>) {
                                "Cannot reduce $operation as part of an insert sequence because " +
                                    "it is not an add action."
                            }

                            operation.item
                        }
                        .toList()
                )
            }
            is Move -> {
                MoveRange(
                    fromIndex = startOperation.fromIndex,
                    toIndex = startOperation.toIndex,
                    itemCount = sequenceEndIndex - sequenceStartIndex
                )
            }
            else -> throw IllegalArgumentException(
                "Cannot reduce sequence starting with $startOperation"
            )
        }
    }

    private fun <T> DiffOperation<T>.canBeCombinedWith(
        otherOperation: DiffOperation<T>,
        offset: Int
    ): Boolean {
        return when (this) {
            is Remove -> otherOperation is Remove && index == otherOperation.index
            is Add -> otherOperation is Add && index + offset == otherOperation.index
            is Move -> otherOperation is Move && toIndex + offset == otherOperation.toIndex
            else -> false
        }
    }

}
