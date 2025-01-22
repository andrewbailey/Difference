package dev.andrewbailey.diff.impl

import dev.andrewbailey.diff.impl.MyersDiffOperation.Delete
import dev.andrewbailey.diff.impl.MyersDiffOperation.Insert
import dev.andrewbailey.diff.impl.MyersDiffOperation.Skip
import kotlin.test.Test
import kotlin.test.assertEquals

class MyersDiffAlgorithmTest {

    @Test
    fun generateDiff_withEmptyInputs_returnsEmptySequence() {
        assertEquals(
            expected = emptyList(),
            actual = MyersDiffAlgorithm<Nothing>(
                original = emptyList(),
                updated = emptyList()
            ).generateDiff().toList()
        )
    }

    @Test
    fun generateDiff_withEmptyStart_returnsAdditions() {
        assertEquals(
            expected = listOf(Insert("A"), Insert("B"), Insert("C")),
            actual = MyersDiffAlgorithm(
                original = emptyList(),
                updated = listOf("A", "B", "C")
            ).generateDiff().toList()
        )
    }

    @Test
    fun generateDiff_withEmptyEnd_returnsDeletions() {
        assertEquals(
            expected = listOf(Delete, Delete, Delete),
            actual = MyersDiffAlgorithm(
                original = listOf("A", "B", "C"),
                updated = emptyList()
            ).generateDiff().toList()
        )
    }

    @Test
    fun generateDiff_withSameStartAndEnd_returnsSkips() {
        assertEquals(
            expected = listOf(Skip, Skip, Skip, Skip),
            actual = MyersDiffAlgorithm(
                original = listOf("A", "B", "C", "D"),
                updated = listOf("A", "B", "C", "D")
            ).generateDiff().toList()
        )
    }

    @Test
    fun generateDiff_withSimpleExample() {
        val original = "ABCABAC".toList()
        val updated = "CBABAC".toList()

        val diff = MyersDiffAlgorithm(original, updated).generateDiff().toList()

        assertEquals(
            expected = listOf(
                Delete,
                Delete,
                Skip,
                Insert(value = 'B'),
                Skip,
                Skip,
                Skip,
                Skip
            ),
            actual = diff
        )
    }

    @Test
    fun generateDiff_withDnaExample() {
        val original = "tgtcgctctcaagatggcgtcttattacgaaaggagccagtccgggttgc".toList()
        val updated = "ggctggggttttcgcacggcgctccctccgcggttgtatctcaggcgaca".toList()

        val diff = MyersDiffAlgorithm(original, updated).generateDiff()

        assertEquals(
            message = "Applying the diff to the input did not yield the updated value.",
            expected = updated,
            actual = applyDiff(original, diff)
        )

        assertEquals(
            message = "The number of operations in the diff did not match the expected value.",
            expected = 40,
            actual = diff.count { it !is Skip }
        )
    }

    @Test
    fun generateDiff_withLoremIpsumExample() {
        val original = (
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod " +
                "tempor incididunt ut labore et dolore magna aliqua. Quis auctor elit sed " +
                "vulputate mi sit amet mauris commodo. Nec dui nunc mattis enim ut tellus " +
                "elementum. Ultricies integer quis auctor elit sed vulputate mi sit amet. " +
                "Ullamcorper velit sed ullamcorper morbi tincidunt."
            ).lowercase().split(" ")
        val updated = (
            "Malesuada fames ac turpis egestas. Varius sit amet mattis vulputate enim. Nisl " +
                "nisi scelerisque eu ultrices vitae auctor eu augue. Sit amet volutpat consequat " +
                "mauris nunc congue nisi vitae. Egestas purus viverra accumsan in nisl nisi " +
                "scelerisque eu. Lobortis elementum nibh tellus molestie. Nulla at volutpat diam " +
                "ut venenatis tellus in metus. Ac turpis egestas sed tempus urna et pharetra " +
                "pharetra massa. Etiam sit amet nisl purus in mollis. Vivamus arcu felis " +
                "bibendum ut tristique et egestas quis. Vestibulum lorem sed risus ultricies " +
                "tristique nulla aliquet. Nunc scelerisque viverra mauris in aliquam. Facilisis " +
                "magna etiam tempor orci eu lobortis elementum nibh. Purus faucibus ornare " +
                "suspendisse sed nisi. Dui accumsan sit amet nulla."
            ).lowercase().split(" ")

        val diff = MyersDiffAlgorithm(original, updated).generateDiff()

        assertEquals(
            message = "Applying the diff to the input did not yield the updated value.",
            expected = updated,
            actual = applyDiff(original, diff)
        )

        assertEquals(
            message = "The number of operations in the diff did not match the expected value.",
            expected = 143,
            actual = diff.count { it !is Skip }
        )
    }

    private fun <T> applyDiff(original: List<T>, diff: Sequence<MyersDiffOperation<T>>): List<T> =
        original.toMutableList().apply {
            var index = 0
            diff.forEach { operation ->
                when (operation) {
                    is Skip -> index++
                    is Insert -> add(index++, operation.value)
                    is Delete -> removeAt(index)
                }
            }
        }
}
