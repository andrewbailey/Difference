@file:JvmName("Difference")

package dev.andrewbailey.diff

import kotlin.jvm.JvmName

/**
 * Constructs a diff between the [original] and [updated] list inputs. The returned [DiffResult]
 * represents a sequence of operations which, if applied in order on the [original] list will
 * yield the [updated] list. The returned list always contains the minimum number of operations
 * required to transform the original input to the updated input.
 *
 * Optionally, move operations can be enabled or disabled by specifying [detectMoves]. If disabled,
 * the diff result will only include add and delete operations, which may cause the same item to
 * be deleted and re-inserted. If movement detection is enabled, all objects shared between the
 * two lists will either remain in place or be moved within the list instead of removing and
 * reinserting them.
 *
 * Internally, this function performs the Eugene-Myers diffing algorithm. the worst case runtime
 * of the algorithm takes on the order of O((M+N)×D + D log D) operations. M and N are the lengths
 * of the two input lists, and D is the smallest number of operations that it takes to modify the
 * original list into the updated one. If move detection is enabled, add another O(D²) to that
 * runtime.
 *
 * @param original The "before" state of the list to be diffed. For optimal performance, it is
 * critical that this data structure supports efficient random reads.
 * @param updated The "after" state of the list to be diffed. For optimal performance, it is
 * critical that this data structure supports efficient random reads.
 * @param detectMoves Whether or not to detect moved objects as such. When disabled, the returned
 * diff will only contain insert and delete operations. Enabled by default.
 * @return A [DiffResult] containing a shortest possible sequence of operations that can transform
 * the "before" state of the list into the "after" state of the list.
 */
fun <T> differenceOf(
    original: List<T>,
    updated: List<T>,
    detectMoves: Boolean = true
) = DiffGenerator.generateDiff(original, updated, detectMoves)
