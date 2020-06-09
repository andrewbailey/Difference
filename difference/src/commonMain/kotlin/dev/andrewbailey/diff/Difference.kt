@file:JvmName("Difference")

package dev.andrewbailey.diff

import kotlin.jvm.JvmName

fun <T> differenceOf(
    original: List<T>,
    updated: List<T>,
    detectMoves: Boolean = true
) = DiffGenerator.generateDiff(original, updated, detectMoves)
