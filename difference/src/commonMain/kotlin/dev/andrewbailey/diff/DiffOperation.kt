package dev.andrewbailey.diff

/**
 * An individual operation contained in a [DiffResult] between two collections.
 */
sealed class DiffOperation<T> {

    /**
     * An operation in a diff to indicate the removal of an object between the before and after
     * states of the collections.
     *
     * @param index The index of the removal, expressed as the index to remove from the collection
     * if you had applied all previous operations in this diff.
     * @param item The item that was removed from the original collection
     */
    data class Remove<T>(
        val index: Int,
        val item: T
    ) : DiffOperation<T>()

    /**
     * An operation in a diff to indicate the removal of a range of objects between the before and
     * after states of the collections.
     *
     * @param startIndex The start index of the removal (inclusive), expressed as the index to
     * remove from the collection if you had applied all previous operations in this diff.
     * @param endIndex The end index of the removal (exclusive), expressed as the index to remove
     * from the collection if you had applied all previous operations in this diff.
     */
    data class RemoveRange<T>(
        val startIndex: Int,
        val endIndex: Int
    ) : DiffOperation<T>()

    /**
     * An operation in a diff to indicate the insertion of an object between the before and after
     * states of the collections.
     *
     * @param index The index of the insertion, expressed as the index to add the given object to
     * the collection if you had applied all previous operations in this diff.
     * @param item The object to insert at the specified location.
     */
    data class Add<T>(
        val index: Int,
        val item: T
    ) : DiffOperation<T>()

    /**
     * An operation in a diff to indicate the insertion of several objects between the before and
     * after states of the collections.
     *
     * @param index The index of the insertions, expressed as the index to add the given object to
     * the collection if you had applied all previous operations in this diff. The first object in
     * the given items list should be inserted at this index, with all subsequent operations
     * following consecutively.
     * @param items The objects to insert at the specified location. Always contains at least two
     * values.
     */
    data class AddAll<T>(
        val index: Int,
        val items: List<T>
    ) : DiffOperation<T>()

    /**
     * An operation in a diff to indicate the movement of one object to a new index between the
     * before and after states of the collections. Only included in a diff if movement detection
     * was enabled.
     *
     * The indices expressed in this object assume applying the move operation as an atomic
     * operation to a collection that has had all other operations in this diff applied. To
     * implement as a remove-then-add call, correct the [toIndex] as shown:
     *
     * ```kotlin
     * add(removeAt(fromIndex), if (toIndex < fromIndex) toIndex else toIndex - 1)
     * ```
     *
     * @param fromIndex The index of the object as it would appear if you had applied all previous
     * operations in this diff.
     * @param toIndex The new index that this object should appear at, not accounting for the
     * deletion of this object from its current location.
     */
    data class Move<T>(
        val fromIndex: Int,
        val toIndex: Int
    ) : DiffOperation<T>()

    /**
     * An operation in a diff to indicate the movement of a range of objects to a new location
     * between the before and after states of the collections. Only included in a diff if movement
     * detection was enabled.
     *
     * The indices expressed in this object assume applying the move operation as an atomic
     * operation to a collection that has had all other operations in this diff applied. To
     * implement as a removeRange-then-add call, correct the [toIndex] as shown:
     *
     * ```kotlin
     * addAll(
     *     values = removeRange(fromIndex, itemCount),
     *     index = if (toIndex < fromIndex) toIndex else toIndex - 1
     * )
     * ```
     *
     * Alternatively, to implement as a loop of remove-then-add calls, correct the destination
     * indices as shown:
     *
     * ```kotlin
     *  when {
     *      toIndex < fromIndex -> {
     *          (0 until count).forEach { item ->
     *              val oldIndex = fromIndex + item
     *              val newIndex = toIndex + item
     *              add(
     *                  item = removeAt(oldIndex),
     *                  index = if (newIndex < oldIndex) newIndex else newIndex - 1
     *              )
     *          }
     *      }
     *      toIndex > fromIndex -> {
     *          repeat(count) {
     *              add(
     *                  item = removeAt(fromIndex),
     *                  index = newIndex
     *              )
     *          }
     *      }
     *  }
     * ```
     *
     * @param fromIndex The index of the object as it would appear if you had applied all previous
     * operations in this diff.
     * @param toIndex The new index that this object should appear at, not accounting for the
     * deletion of this object from its current location.
     */
    data class MoveRange<T>(
        val fromIndex: Int,
        val toIndex: Int,
        val itemCount: Int
    ) : DiffOperation<T>()
}
