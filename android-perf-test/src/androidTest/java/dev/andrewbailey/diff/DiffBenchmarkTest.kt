package dev.andrewbailey.diff

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.random.Random
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiffBenchmarkTest {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun verySmallDiffWithoutMoves() = runBenchmarkScenario(
        seed = 4459894567797046261,
        numberOfItems = 100,
        numberOfOperations = 10,
        detectMoves = false
    )

    @Test
    fun verySmallDiffWithMoves() = runBenchmarkScenario(
        seed = 4459894567797046261,
        numberOfItems = 100,
        numberOfOperations = 10,
        detectMoves = true
    )

    @Test
    fun smallDiffWithoutMoves() = runBenchmarkScenario(
        seed = -5791837575014754264,
        numberOfItems = 1000,
        numberOfOperations = 100,
        detectMoves = false
    )

    @Test
    fun smallDiffWithMoves() = runBenchmarkScenario(
        seed = -5791837575014754264,
        numberOfItems = 1000,
        numberOfOperations = 100,
        detectMoves = true
    )

    @Test
    fun mediumDiffWithoutMoves() = runBenchmarkScenario(
        seed = -797670750388632780,
        numberOfItems = 5000,
        numberOfOperations = 500,
        detectMoves = false
    )

    @Test
    fun mediumDiffWithMoves() = runBenchmarkScenario(
        seed = -797670750388632780,
        numberOfItems = 5000,
        numberOfOperations = 500,
        detectMoves = true
    )

    @Test
    fun largeDiffWithoutMoves() = runBenchmarkScenario(
        seed = 8208385239328551378,
        numberOfItems = 10000,
        numberOfOperations = 1000,
        detectMoves = false
    )

    @Test
    fun largeDiffWithMoves() = runBenchmarkScenario(
        seed = 8208385239328551378,
        numberOfItems = 10000,
        numberOfOperations = 1000,
        detectMoves = true
    )

    private fun runBenchmarkScenario(
        seed: Long,
        numberOfItems: Int,
        numberOfOperations: Int,
        detectMoves: Boolean
    ) {
        val original = generateList(
            numberOfItems = numberOfItems,
            seed = seed
        )

        val updated = generateModifiedList(
            originalData = original,
            numberOfOperations = numberOfOperations,
            seed = seed
        )

        benchmarkRule.measureRepeated {
            differenceOf(
                original = original,
                updated = updated,
                detectMoves = detectMoves
            )
        }
    }

    private fun generateList(numberOfItems: Int, seed: Long): List<Int> {
        val random = Random(seed)
        return List(numberOfItems) { random.nextInt() }
    }

    private fun generateModifiedList(
        originalData: List<Int>,
        numberOfOperations: Int,
        seed: Long
    ): List<Int> {
        val random = Random(seed)
        val modifiedList = originalData.toMutableList()

        repeat(numberOfOperations) {
            when (random.nextInt(0, 3)) {
                0 -> { // Delete
                    modifiedList.removeAt(
                        index = random.nextInt(0, modifiedList.size)
                    )
                }
                1 -> { // Insert
                    modifiedList.add(
                        index = random.nextInt(0, modifiedList.size + 1),
                        element = random.nextInt()
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
