package dev.andrewbailey.diff

import dev.andrewbailey.diff.DiffOperation.*
import kotlin.test.Test
import kotlin.test.assertEquals

class DiffGeneratorTest {

    @Test
    fun `generateDiff with empty input returns empty result`() {
        val original = emptyList<String>()
        val updated = emptyList<String>()

        val diff = DiffGenerator.generateDiff(original, updated)

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

        val diff = DiffGenerator.generateDiff(original, updated)

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

        val diff = DiffGenerator.generateDiff(original, updated)

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

        val diff = DiffGenerator.generateDiff(original, updated)

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

        val diff = DiffGenerator.generateDiff(original, updated)

        assertEquals(
            message = "The returned diff did not match the expected value.",
            expected = DiffResult(listOf(
                Remove(index = 0),
                RemoveRange(startIndex = 2, endIndex = 5),
                Add(index = 2, item = 'A'),
                Add(index = 5, item = 'I'),
                Remove(index = 7),
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

    private fun <T> applyDiff(original: List<T>, diff: DiffResult<T>): List<T> {
        return original.toMutableList().apply {
            diff.applyDiff(
                remove = { index -> removeAt(index) },
                insert = { item, index -> add(index, item) },
                move = { oldIndex, newIndex -> add(newIndex, removeAt(oldIndex)) }
            )
        }
    }

}
