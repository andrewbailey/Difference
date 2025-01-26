package dev.andrewbailey.diff.impl

import dev.andrewbailey.diff.impl.MyersDiffOperation.Delete
import dev.andrewbailey.diff.impl.MyersDiffOperation.Insert
import dev.andrewbailey.diff.impl.MyersDiffOperation.Skip
import kotlin.math.ceil

/**
 * This class implements a variation of Eugene Myers' Diffing Algorithm that uses linear space. This
 * algorithm has a worst-case runtime of O(M + N + D^2), where N and M are the lengths of [original]
 * and [updated], and D is the length of the edit script (i.e. the number of `Insert`s and `Delete`s
 * that appear in the result of [generateDiff]). This algorithm has an expected runtime of
 * O((M + N) D) and always uses O(D) space.
 *
 * The basic variation of this algorithm (which uses quadratic space) is a greedy algorithm. In this
 * introductory variation, the algorithm places the inputs on a grid. At (0, 0), no inputs from
 * either [original] or [updated] have been accepted. Moving to the right (in the positive X
 * direction) indicates that the next item in [original] should be removed. Moving downward (in the
 * positive Y direction) indicates that the next item in [updated] should be inserted. Moving
 * diagonally (in both the positive X and Y direction) indicates that [original] and [updated] share
 * a value, so there is no difference.
 *
 * The greedy algorithm makes multiple passes to find the shortest path from (0, 0) to (N, M). When
 * the algorithm runs through the inputs, it can move diagonally for free. Long chains of diagonals
 * where both inputs have a matching subsequence are called a [Snake]. In each iteration, the
 * possible paths expand either by one unit in a single direction or, if there is a snake,
 * diagonally until the end of the snake.
 *
 * The linear-space implementation of this algorithm is a divide and conquer algorithm that follows
 * the same basic principles as the greedy algorithm. The input is traversed forwards and backwards
 * simultaneously in a subset of the grid. When the paths in both directions intersect, a shortest
 * path has been found and a diff can be outputted.
 *
 * You can read the technical paper by Eugene Myers here: http://xmailserver.org/diff2.pdf
 */
internal class MyersDiffAlgorithm<T>(
    private val original: List<T>,
    private val updated: List<T>
) {

    fun generateDiff(): List<MyersDiffOperation<T>> {
        val path = findPath()
        val regions = mutableListOf<MyersDiffOperation<T>>()

        path.fastForEach { (p1, p2) ->
            var (x1, y1) = walkDiagonal(p1, p2, regions)
            val (x2, y2) = p2

            val dY = y2 - y1
            val dX = x2 - x1
            when {
                dY > dX -> {
                    regions += interpretRegion(x1, y1, x1, y1 + 1)
                    y1++
                }
                dY < dX -> {
                    regions += interpretRegion(x1, y1, x1 + 1, y1)
                    x1++
                }
            }

            walkDiagonal(Point(x1, y1), p2, regions)
        }

        return regions
    }

    private fun walkDiagonal(
        start: Point,
        end: Point,
        regionsOutput: MutableList<MyersDiffOperation<T>>
    ): Point {
        var (x1, y1) = start
        val (x2, y2) = end
        while (x1 < x2 && y1 < y2 && original[x1] == updated[y1]) {
            regionsOutput += interpretRegion(x1, y1, x1 + 1, y1 + 1)
            x1++
            y1++
        }

        return Point(x1, y1)
    }

    private fun interpretRegion(
        x1: Int,
        y1: Int,
        x2: Int,
        y2: Int
    ): MyersDiffOperation<T> = when {
        x1 == x2 -> Insert(value = updated[y1])
        y1 == y2 -> Delete
        else -> Skip
    }

    private fun findPath(): List<Snake> {
        val snakes = mutableListOf<Snake>()
        val stack = mutableListOf<Region>()

        stack.push(
            Region(
                left = 0,
                top = 0,
                right = original.size,
                bottom = updated.size
            )
        )

        while (stack.isNotEmpty()) {
            val region = stack.pop()

            val snake = midpoint(region)
            if (snake != null) {
                snakes += snake
                val (start, finish) = snake

                stack.push(
                    region.copy(
                        right = start.x,
                        bottom = start.y
                    )
                )

                stack.push(
                    region.copy(
                        left = finish.x,
                        top = finish.y
                    )
                )
            }
        }

        snakes.sort()
        return snakes
    }

    private fun midpoint(region: Region): Snake? {
        if (region.size == 0) {
            return null
        }

        val max = ceil(region.size / 2.0f).toInt()

        val vForwards = CircularIntArray(2 * max + 1)
        vForwards[1] = region.left
        val vBackwards = CircularIntArray(2 * max + 1)
        vBackwards[1] = region.bottom

        for (depth in 0..max) {
            forwards(region, vForwards, vBackwards, depth)?.let { return it }
            backwards(region, vForwards, vBackwards, depth)?.let { return it }
        }

        return null
    }

    private fun forwards(
        region: Region,
        vForwards: CircularIntArray,
        vBackwards: CircularIntArray,
        depth: Int
    ): Snake? {
        // This loop is effectively `for (k in (-depth..depth step 2).reversed())`, but avoids
        // allocating a Range object.
        var k = depth
        while (k >= -depth) {
            val c = k - region.delta

            var endX: Int
            val startX: Int

            if (k == -depth || (k != -depth && vForwards[k - 1] < vForwards[k + 1])) {
                startX = vForwards[k + 1]
                endX = startX
            } else {
                startX = vForwards[k - 1]
                endX = startX + 1
            }

            var endY = region.top + (endX - region.left) - k
            val startY = if (depth == 0 || endX != startX) endY else endY - 1

            while (endX < region.right &&
                endY < region.bottom &&
                original[endX] == updated[endY]
            ) {
                endX++
                endY++
            }

            vForwards[k] = endX

            if (region.delta.isOdd() && c in -(depth - 1) until depth && endY >= vBackwards[c]) {
                return Snake(
                    start = Point(startX, startY),
                    end = Point(endX, endY)
                )
            }

            k -= 2
        }

        return null
    }

    private fun backwards(
        region: Region,
        vForwards: CircularIntArray,
        vBackwards: CircularIntArray,
        depth: Int
    ): Snake? {
        // This loop is effectively `for (c in (-depth..depth step 2).reversed())`, but avoids
        // allocating a Range object.
        var c = depth
        while (c >= -depth) {
            val k = c + region.delta

            val endY: Int
            var startY: Int

            if (c == -depth || (c != depth && vBackwards[c - 1] > vBackwards[c + 1])) {
                endY = vBackwards[c + 1]
                startY = endY
            } else {
                endY = vBackwards[c - 1]
                startY = endY - 1
            }

            var startX = region.left + (startY - region.top) + k
            val endX = if (depth == 0 || startY != endY) startX else startX + 1

            while (startX > region.left &&
                startY > region.top &&
                original[startX - 1] == updated[startY - 1]
            ) {
                startX--
                startY--
            }

            vBackwards[c] = startY

            if (region.delta.isEven() && k in -depth..depth && startX <= vForwards[k]) {
                return Snake(
                    start = Point(startX, startY),
                    end = Point(endX, endY)
                )
            }

            c -= 2
        }

        return null
    }
}
