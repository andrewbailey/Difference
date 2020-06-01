package dev.andrewbailey.diff

class DiffResult<T> internal constructor(
    val operations: List<DiffOperation<T>>
) {

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

    override fun equals(other: Any?) =
        other is DiffResult<*> && other.operations == operations

    override fun hashCode() =
        operations.hashCode()

    override fun toString() =
        "DiffResult(operations = $operations)"

}
