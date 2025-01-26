package dev.andrewbailey.diff

/**
 * Stores the result of a diff calculated by [differenceOf].
 */
class DiffResult<T> internal constructor(
    /**
     * The sequence of operations in the diff. When applied in order, these operations will
     * transform the original collection into the target collection. This list always coalesces
     * ranges of the same operation together, and prefers to use the minimum number of
     * [DiffOperation] objects required to express the diff.
     */
    val operations: List<DiffOperation<T>>
) {

    /**
     * Executes the operations in this diff-in order. This function is generally intended to
     * mirror the state changes expressed in this difference to a parallel data structure derived
     * by the diff-ed collection.
     *
     * The lambdas passed into this method are invoked to respond to each operation. When an index
     * is specified, it expresses the index of the in-flight data structure. That is, an index
     * which is only valid when all previously dispatched operations have been applied to the
     * original collection.
     *
     * This overload applies operations on an element-by-element basis. Use the overload with
     * the additional "Range" lambdas to support bulk operations, which can improve performance.
     *
     * @param remove Called to remove the object at the specified `index` from the in-flight
     * collection as a step towards the final collection.
     * @param insert Called to insert the given `item` at the specified `index` to the in-flight
     * collection as a step towards the final collection.
     * @param move If this diff was generated with moves detected, this function is called to move
     * the item at `oldIndex` to `newIndex`. If this diff was not generated with moves detected,
     * this lambda will never be invoked. These indexes are written assuming an atomic operation
     * to perform the move. To implement a move operation as a remove and re-insert operation, the
     * `newIndex` may need to be corrected with an offset. In practice, this looks like
     * `add(removeAt(oldIndex), if (newIndex < oldIndex) else newIndex - 1)`.
     */
    inline fun applyDiff(
        crossinline remove: (index: Int) -> Unit,
        crossinline insert: (item: T, index: Int) -> Unit,
        crossinline move: (oldIndex: Int, newIndex: Int) -> Unit
    ) {
        applyDiff(
            remove = remove,
            insert = insert,
            move = move,
            removeRange = { start, end ->
                repeat(times = end - start) {
                    remove(start)
                }
            },
            insertAll = { items, index ->
                items.forEachIndexed { itemIndex, item ->
                    insert(item, index + itemIndex)
                }
            },
            moveRange = { oldIndex, newIndex, count ->
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
        )
    }

    /**
     * Executes the operations in this diff-in order, with support for applying operations in bulk
     * when a large span of items experience the same transformation. This function is generally
     * intended to mirror the state changes expressed in this difference to a parallel data
     * structure derived by the diff-ed collection.
     *
     * The lambdas passed into this method are invoked to respond to each aggregated operation.
     * When an index or range is specified, it is indexed to the in-flight data structure. That is,
     * an index which is only valid when all previously dispatched operations have been applied
     * to the original collection.
     *
     * @param remove Called to remove the object at the specified `index` from the in-flight
     * collection as a step towards the final collection.
     * @param removeRange Called to remove all objects between index `start` (inclusive) and `end`
     * (exclusive) from the in-flight collection as a step towards the final collection.
     * @param insert Called to insert the given `item` at the specified `index` to the in-flight
     * collection as a step towards the final collection.
     * @param insertAll Called to insert all items in the provided collection to the in-flight
     * collection. The given list of items should be added in-order with the inserted sequence
     * starting at the provided `index` in the in-flight collection.
     * @param move If this diff was generated with moves detected, this function is called to move
     * the item at `oldIndex` to `newIndex`. If this diff was not generated with moves detected,
     * this lambda will never be invoked. These indexes are written assuming an atomic operation
     * to perform the move. To implement a move operation as a remove and re-insert operation, the
     * `newIndex` may need to be corrected with an offset. In practice, this looks like
     * `add(removeAt(oldIndex), if (newIndex < oldIndex) newIndex else newIndex - 1)`.
     * @param moveRange This lambda is used in the same way as [move] is, but operates on a
     * contiguous span of two or more items to be moved. The indexing works in the same way as it
     * does for the single-item variant, in that the `newIndex` and `oldIndex` arguments provided
     * are both specified with respect to the current state of the in-flight array. If implementing
     * this expression as an addRange and removeRange operation, or by breaking this operation into
     * multiple move operations, you will need to correct the destination index when it is before
     * the source index.
     */
    inline fun applyDiff(
        crossinline remove: (index: Int) -> Unit,
        crossinline removeRange: (start: Int, end: Int) -> Unit,
        crossinline insert: (item: T, index: Int) -> Unit,
        crossinline insertAll: (items: List<T>, index: Int) -> Unit,
        crossinline move: (oldIndex: Int, newIndex: Int) -> Unit,
        crossinline moveRange: (oldIndex: Int, newIndex: Int, count: Int) -> Unit
    ) {
        operations.forEach { operation ->
            when (operation) {
                is DiffOperation.Remove -> {
                    remove(operation.index)
                }
                is DiffOperation.RemoveRange -> {
                    removeRange(operation.startIndex, operation.endIndex)
                }
                is DiffOperation.Add -> {
                    insert(operation.item, operation.index)
                }
                is DiffOperation.AddAll -> {
                    insertAll(operation.items, operation.index)
                }
                is DiffOperation.Move -> {
                    move(operation.fromIndex, operation.toIndex)
                }
                is DiffOperation.MoveRange -> {
                    moveRange(operation.fromIndex, operation.toIndex, operation.itemCount)
                }
            }
        }
    }

    override fun equals(other: Any?) = other is DiffResult<*> && other.operations == operations

    override fun hashCode() = operations.hashCode()

    override fun toString() = "DiffResult(operations = $operations)"
}
