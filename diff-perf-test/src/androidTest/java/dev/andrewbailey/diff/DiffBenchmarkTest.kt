package dev.andrewbailey.diff

import android.Manifest
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class DiffBenchmarkTest {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun verySmallDiff() {
        val original = generateList(
            numberOfItems = 100,
            seed = 4459894567797046261
        )

        val updated = generateModifiedList(
            originalData = original,
            numberOfOperations = 10,
            seed = 4714230579337615937
        )

        benchmarkRule.measureRepeated {
            DiffGenerator.generateDiff(
                original = original,
                updated = updated
            )
        }
    }

    @Test
    fun smallDiff() {
        val original = generateList(
            numberOfItems = 1000,
            seed = -5791837575014754264
        )

        val updated = generateModifiedList(
            originalData = original,
            numberOfOperations = 100,
            seed = -2363742209552295760
        )

        benchmarkRule.measureRepeated {
            DiffGenerator.generateDiff(
                original = original,
                updated = updated
            )
        }
    }

    @Test
    fun mediumDiff() {
        val original = generateList(
            numberOfItems = 5000,
            seed = -797670750388632780
        )

        val updated = generateModifiedList(
            originalData = original,
            numberOfOperations = 500,
            seed = -5784637514750304832
        )

        benchmarkRule.measureRepeated {
            DiffGenerator.generateDiff(
                original = original,
                updated = updated
            )
        }
    }

    @Test
    fun largeDiff() {
        val original = generateList(
            numberOfItems = 10000,
            seed = 8208385239328551378
        )

        val updated = generateModifiedList(
            originalData = original,
            numberOfOperations = 1000,
            seed = 3794317317428887769
        )

        benchmarkRule.measureRepeated {
            DiffGenerator.generateDiff(
                original = original,
                updated = updated
            )
        }
    }

    private fun generateList(
        numberOfItems: Int,
        seed: Long
    ): List<Int> {
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
