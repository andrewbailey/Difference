package dev.andrewbailey.diff

import dev.andrewbailey.diff.DiffOperation.Add
import dev.andrewbailey.diff.DiffOperation.AddAll
import dev.andrewbailey.diff.DiffOperation.Move
import dev.andrewbailey.diff.DiffOperation.MoveRange
import dev.andrewbailey.diff.DiffOperation.Remove
import dev.andrewbailey.diff.DiffOperation.RemoveRange
import dev.andrewbailey.diff.impl.fastForEach
import dev.andrewbailey.diff.impl.fastForEachIndexed

/**
 * This class serves as a convenience class for Java users who may find it tedious to call
 * [DiffResult.applyDiff], since Java users have to rely on Kotlin's `FunctionN` interfaces and
 * don't have access to Kotlin's named arguments and default arguments.
 *
 * If you're using Difference directly in Kotlin, then there's no reason for you to use this class
 * since the [DiffResult.applyDiff] is a more idiomatic way to use a difference result.
 */
abstract class DiffReceiver<T> {

    fun applyDiff(diff: DiffResult<T>) {
        diff.operations.fastForEach { operation ->
            when (operation) {
                is Remove -> {
                    remove(operation.index)
                }
                is RemoveRange -> {
                    removeRange(operation.startIndex, operation.endIndex)
                }
                is Add -> {
                    insert(operation.item, operation.index)
                }
                is AddAll -> {
                    insertAll(operation.items, operation.index)
                }
                is Move -> {
                    move(operation.fromIndex, operation.toIndex)
                }
                is MoveRange -> {
                    moveRange(operation.fromIndex, operation.toIndex, operation.itemCount)
                }
            }
        }
    }

    abstract fun remove(index: Int)

    open fun removeRange(start: Int, end: Int) {
        repeat(times = end - start) {
            remove(start)
        }
    }

    abstract fun insert(item: T, index: Int)

    open fun insertAll(items: List<T>, index: Int) {
        items.fastForEachIndexed { itemIndex, item ->
            insert(item, index + itemIndex)
        }
    }

    open fun move(oldIndex: Int, newIndex: Int): Unit = throw UnsupportedOperationException(
        "The received diff included move operations, but this receiver does not support moving " +
            "elements. You should either disable movement detection when generating the " +
            "diff, or override the `DiffReceiver.move()` function."
    )

    open fun moveRange(
        oldIndex: Int,
        newIndex: Int,
        count: Int
    ) {
        when {
            newIndex < oldIndex -> {
                (0 until count).forEach { item ->
                    move(oldIndex + item, newIndex + item)
                }
            }
            newIndex > oldIndex -> {
                repeat(count) {
                    move(oldIndex, newIndex)
                }
            }
        }
    }
}
