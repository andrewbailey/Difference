package dev.andrewbailey.diff

import dev.andrewbailey.diff.DiffOperation.*
import kotlin.test.Test
import kotlin.test.assertEquals

class DiffGeneratorTest {

    @Test
    fun `generateDiff with empty input returns empty result`() {
        val original = emptyList<String>()
        val updated = emptyList<String>()

        val diff = DiffGenerator.generateDiff(original, updated, false)

        assertEquals(
            message = "The returned diff did not match the expected value.",
            expected = DiffResult(emptyList()),
            actual = diff
        )

        assertEquals(
            message = "Applying the diff to the input did not yield the updated value.",
            expected = updated,
            actual = applyDiff(original, diff)
        )
    }

    @Test
    fun `generateDiff with empty start returns additions`() {
        val original = emptyList<String>()
        val updated = listOf("A", "B", "C")

        val diff = DiffGenerator.generateDiff(original, updated, false)

        assertEquals(
            message = "The returned diff did not match the expected value.",
            expected = DiffResult(listOf(
                AddAll(
                    index = 0,
                    items = listOf("A", "B", "C")
                )
            )),
            actual = diff
        )

        assertEquals(
            message = "Applying the diff to the input did not yield the updated value.",
            expected = updated,
            actual = applyDiff(original, diff)
        )
    }

    @Test
    fun `generateDiff with empty end returns deletions`() {
        val original = listOf("A", "B", "C")
        val updated = emptyList<String>()

        val diff = DiffGenerator.generateDiff(original, updated, false)

        assertEquals(
            message = "The returned diff did not match the expected value.",
            expected = DiffResult(listOf(
                RemoveRange(
                    startIndex = 0,
                    endIndex = 3
                )
            )),
            actual = diff
        )

        assertEquals(
            message = "Applying the diff to the input did not yield the updated value.",
            expected = updated,
            actual = applyDiff(original, diff)
        )
    }

    @Test
    fun `generateDiff with same start and end returns empty diff`() {
        val original = listOf("A", "B", "C")
        val updated = listOf("A", "B", "C")

        val diff = DiffGenerator.generateDiff(original, updated, false)

        assertEquals(
            message = "The returned diff did not match the expected value.",
            expected = DiffResult(emptyList()),
            actual = diff
        )

        assertEquals(
            message = "Applying the diff to the input did not yield the updated value.",
            expected = updated,
            actual = applyDiff(original, diff)
        )
    }

    @Test
    fun `generateDiff without moves calculates complex diff`() {
        val original = "ABCDEFGHJKLPQR".toList()
        val updated = "BCAGHIJLMNOPQR".toList()

        val diff = DiffGenerator.generateDiff(
            original = original,
            updated = updated,
            detectMoves = false
        )

        assertEquals(
            message = "The returned diff did not match the expected value.",
            expected = DiffResult(listOf(
                Remove(index = 0, item = 'A'),
                RemoveRange(startIndex = 2, endIndex = 5),
                Add(index = 2, item = 'A'),
                Add(index = 5, item = 'I'),
                Remove(index = 7, item = 'K'),
                AddAll(index = 8, items = "MNO".toList())
            )),
            actual = diff
        )

        assertEquals(
            message = "Applying the diff to the input did not yield the updated value.",
            expected = updated,
            actual = applyDiff(original, diff)
        )
    }

    @Test
    fun `generateDiff detects forwards and backwards movements`() {
        val original = "CADEFB".toList()
        val updated = "ABCDEF".toList()

        val diff = DiffGenerator.generateDiff(
            original = original,
            updated = updated,
            detectMoves = true
        )

        assertEquals(
            message = "The returned diff did not match the expected value.",
            expected = DiffResult(listOf(
                Move(
                    fromIndex = 0,
                    toIndex = 2
                ),
                Move(
                    fromIndex = 5,
                    toIndex = 1
                )
            )),
            actual = diff
        )

        assertEquals(
            message = "Applying the diff to the input did not yield the updated value.",
            expected = updated,
            actual = applyDiff(original, diff)
        )
    }

    @Test
    fun `generateDiff detects move forwards sequences`() {
        val original = "ABCDEFGHIJKL".toList()
        val updated = "ABCGHIJKLDEF".toList()

        val diff = DiffGenerator.generateDiff(
            original = original,
            updated = updated,
            detectMoves = true
        )

        assertEquals(
            message = "The returned diff did not match the expected value.",
            expected = DiffResult(listOf(
                MoveRange(
                    fromIndex = 3,
                    toIndex = 12,
                    itemCount = 3
                )
            )),
            actual = diff
        )

        assertEquals(
            message = "Applying the diff to the input did not yield the updated value.",
            expected = updated,
            actual = applyDiff(original, diff)
        )
    }

    @Test
    fun `generateDiff detects move backwards sequences`() {
        val original = "ABCDEFGHIJKL".toList()
        val updated = "HIJABCDEFGKL".toList()

        val diff = DiffGenerator.generateDiff(
            original = original,
            updated = updated,
            detectMoves = true
        )

        assertEquals(
            message = "The returned diff did not match the expected value.",
            expected = DiffResult(listOf(
                MoveRange(
                    fromIndex = 7,
                    toIndex = 0,
                    itemCount = 3
                )
            )),
            actual = diff
        )

        assertEquals(
            message = "Applying the diff to the input did not yield the updated value.",
            expected = updated,
            actual = applyDiff(original, diff)
        )
    }

    @Test
    fun `generateDiff detects adjacent moves to different destinations`() {
        val original = "ABCDEFGHIJKL".toList()
        val updated = "DABCGHEIJKLF".toList()

        val diff = DiffGenerator.generateDiff(
            original = original,
            updated = updated,
            detectMoves = true
        )

        assertEquals(
            message = "The returned diff did not match the expected value.",
            expected = DiffResult(listOf(
                Move(
                    fromIndex = 3,
                    toIndex = 0
                ),
                Move(
                    fromIndex = 4,
                    toIndex = 8
                ),
                Move(
                    fromIndex = 4,
                    toIndex = 12
                )
            )),
            actual = diff
        )

        assertEquals(
            message = "Applying the diff to the input did not yield the updated value.",
            expected = updated,
            actual = applyDiff(original, diff)
        )
    }

    @Test
    fun `generateDiff with moves excludes opposite operations from merging`() {
        val original = listOf(3, 2, 3, 0, 0, 3, 1, 0, 1, 2)
        val updated = listOf(1, 3, 2, 0, 12, 0, 15, 0, 1, 2, 3)

        val diff = DiffGenerator.generateDiff(
            original = original,
            updated = updated,
            detectMoves = true
        )

        assertEquals(
            message = "The returned diff did not match the expected value.",
            expected = DiffResult(listOf(
                Move(
                    fromIndex = 6,
                    toIndex = 0
                ),
                Move(
                    fromIndex = 3,
                    toIndex = 10
                ),
                Add(
                    index = 4,
                    item = 12
                ),
                Remove(
                    index = 6,
                    item = 3
                ),
                Add(
                    index = 6,
                    item = 15
                )
            )),
            actual = diff
        )

        assertEquals(
            message = "Applying the diff to the input did not yield the updated value.",
            expected = updated,
            actual = applyDiff(original, diff)
        )
    }

    private fun <T> applyDiff(original: List<T>, diff: DiffResult<T>): List<T> {
        return original.toMutableList().apply {
            diff.applyDiff(
                remove = { index -> removeAt(index) },
                insert = { item, index -> add(index, item) },
                move = { oldIndex, newIndex ->
                    add(
                        element = removeAt(oldIndex),
                        index = if (newIndex < oldIndex) {
                            newIndex
                        } else {
                            newIndex - 1
                        }
                    )
                }
            )
        }
    }

}
