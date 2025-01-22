package dev.andrewbailey.diff

import kotlin.random.Random
import kotlin.test.assertEquals
import org.junit.Test

class DiffReceiverTest {

    @Test
    fun `applyDiff on original value returns updated value`() {
        val original = generateList()
        val updated = generateModifiedList(original)

        val diff = differenceOf(original, updated, false)

        val appliedResult = original.toMutableList()

        object : DiffReceiver<String>() {
            override fun remove(index: Int) {
                appliedResult.removeAt(index)
            }

            override fun insert(item: String, index: Int) {
                appliedResult.add(index, item)
            }

            override fun move(oldIndex: Int, newIndex: Int) {
                appliedResult.add(
                    element = appliedResult.removeAt(oldIndex),
                    index = if (newIndex < oldIndex) {
                        newIndex
                    } else {
                        newIndex - 1
                    }
                )
            }
        }.applyDiff(diff)

        assertEquals(
            message = "Applying the diff to the input did not yield the updated value.",
            expected = updated,
            actual = appliedResult
        )
    }

    private fun generateList(): List<String> {
        val random = Random(319163183995026179)
        return List(500) { random.nextInt().toString() }
    }

    private fun generateModifiedList(originalData: List<String>): List<String> {
        val random = Random(3260128955430943624)
        val modifiedList = originalData.toMutableList()

        repeat(250) {
            when (random.nextInt(0, 3)) {
                0 -> { // Delete
                    modifiedList.removeAt(
                        index = random.nextInt(0, modifiedList.size)
                    )
                }
                1 -> { // Insert
                    modifiedList.add(
                        index = random.nextInt(0, modifiedList.size + 1),
                        element = random.nextInt().toString()
                    )
                }
                2 -> { // Move
                    val item = modifiedList.removeAt(
                        index = random.nextInt(0, modifiedList.size)
                    )
                    modifiedList.add(
                        index = random.nextInt(0, modifiedList.size + 1),
                        element = item
                    )
                }
            }
        }

        return modifiedList
    }
}
